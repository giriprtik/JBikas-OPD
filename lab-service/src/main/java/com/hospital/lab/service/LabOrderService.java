package com.hospital.lab.service;

import com.hospital.lab.client.OpdClient;
import com.hospital.lab.dto.*;
import com.hospital.lab.exception.BusinessRuleException;
import com.hospital.lab.exception.NotFoundException;
import com.hospital.lab.model.*;
import com.hospital.lab.repository.LabAttachmentRepository;
import com.hospital.lab.repository.LabOrderRepository;
import com.hospital.lab.repository.ResultAmendmentRepository;
import com.hospital.lab.repository.TestDefinitionRepository;
import com.hospital.lab.security.CurrentUser;
import com.hospital.lab.security.StaffPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lab order workflow: create → collect sample → enter results → release → (amend).
 * Flags are always computed here with ResultFlagEvaluator; clients never send flags.
 */
@Service
public class LabOrderService {

    private static final Logger log = LoggerFactory.getLogger(LabOrderService.class);

    /** Upper bound that fits NUMERIC(14,4) and catches obvious typing errors. */
    private static final BigDecimal MAX_ABS_VALUE = new BigDecimal("1000000000");

    private static final Comparator<LabOrder> WORKLIST_ORDER = Comparator
            .comparingInt((LabOrder o) -> o.getPriority().rank())
            .thenComparing(LabOrder::getOrderedAt);

    private final LabOrderRepository orders;
    private final TestDefinitionRepository tests;
    private final ResultAmendmentRepository amendments;
    private final LabAttachmentRepository attachments;
    private final OrderNumberService orderNumbers;
    private final OpdClient opdClient;
    private final LabMapper mapper;
    private final Clock clock;

    public LabOrderService(LabOrderRepository orders, TestDefinitionRepository tests,
                           ResultAmendmentRepository amendments, LabAttachmentRepository attachments,
                           OrderNumberService orderNumbers, OpdClient opdClient,
                           LabMapper mapper, Clock clock) {
        this.orders = orders;
        this.tests = tests;
        this.amendments = amendments;
        this.attachments = attachments;
        this.orderNumbers = orderNumbers;
        this.opdClient = opdClient;
        this.mapper = mapper;
        this.clock = clock;
    }

    // ---------------------------------------------------------------- create

    @Transactional
    public LabOrderResponse create(CreateLabOrderRequest r) {
        List<String> codes = r.testCodes().stream()
                .map(c -> c.trim().toUpperCase())
                .distinct()
                .toList();
        Map<String, TestDefinition> byCode = tests.findByCodeIn(codes).stream()
                .filter(TestDefinition::isActive)
                .collect(Collectors.toMap(TestDefinition::getCode, Function.identity()));
        List<String> unknown = codes.stream().filter(c -> !byCode.containsKey(c)).toList();
        if (!unknown.isEmpty()) {
            throw new BusinessRuleException("Unknown or discontinued test code(s): " + String.join(", ", unknown));
        }

        StaffPrincipal doctor = CurrentUser.get();
        LabOrder order = new LabOrder();
        order.setOrderNumber(orderNumbers.next());
        order.setVisitId(r.visitId());
        order.setPatientId(r.patientId());
        order.setMrn(r.mrn().trim());
        order.setPatientName(r.patientName().trim());
        order.setPatientGender(r.patientGender());
        order.setPatientAgeYears(r.patientAgeYears());
        order.setOrderingDoctor(doctor.username());
        order.setOrderingDoctorName(r.orderingDoctorName() != null ? r.orderingDoctorName() : doctor.fullName());
        order.setPriority(r.priority() != null ? r.priority() : OrderPriority.ROUTINE);
        order.setClinicalNotes(blankToNull(r.clinicalNotes()));
        order.setStatus(LabOrderStatus.ORDERED);
        order.setOrderedAt(Instant.now(clock));

        List<TestDefinition> definitions = codes.stream()
                .map(byCode::get)
                .sorted(Comparator.comparingInt(TestDefinition::getSortOrder))
                .toList();
        int itemOrder = 0;
        for (TestDefinition def : definitions) {
            LabOrderItem item = new LabOrderItem();
            item.setTestCode(def.getCode());
            item.setTestName(def.getName());
            item.setCategory(def.getCategory());
            item.setSpecimen(def.getSpecimen());
            item.setSortOrder(itemOrder++);
            for (TestParameter p : def.getParameters()) {
                item.addResult(newResult(p, r.patientGender(), r.patientAgeYears()));
            }
            order.addItem(item);
        }
        return response(orders.save(order));
    }

    /** Empty result row with the patient's reference limits frozen in. */
    private static LabResult newResult(TestParameter p, String gender, int ageYears) {
        LabResult result = new LabResult();
        result.setParameterCode(p.getCode());
        result.setParameterName(p.getName());
        result.setValueType(p.getValueType());
        result.setUnit(p.getUnit());
        result.setDecimals(p.getDecimals());
        result.setNormalValues(p.getNormalValues());
        result.setSortOrder(p.getSortOrder());
        if (p.getValueType() == ValueType.NUMERIC) {
            ReferenceRangeResolver.resolve(p.getRanges(), gender, ageYears).ifPresent(range -> {
                result.setRefLow(range.low());
                result.setRefHigh(range.high());
                result.setCriticalLow(range.criticalLow());
                result.setCriticalHigh(range.criticalHigh());
            });
        }
        return result;
    }

    // ---------------------------------------------------------------- reads

    /**
     * Worklist. Defaults to open work (ORDERED, SAMPLE_COLLECTED, IN_PROGRESS), most urgent first.
     * RELEASED / CANCELLED, when asked for, are limited to the last 24 hours, newest first.
     */
    @Transactional(readOnly = true)
    public List<LabOrderSummary> worklist(Set<LabOrderStatus> statuses) {
        Set<LabOrderStatus> wanted = statuses == null || statuses.isEmpty()
                ? EnumSet.of(LabOrderStatus.ORDERED, LabOrderStatus.SAMPLE_COLLECTED, LabOrderStatus.IN_PROGRESS)
                : statuses;
        Set<LabOrderStatus> open = EnumSet.copyOf(wanted);
        open.removeAll(EnumSet.of(LabOrderStatus.RELEASED, LabOrderStatus.CANCELLED));
        Set<LabOrderStatus> done = EnumSet.copyOf(wanted);
        done.retainAll(EnumSet.of(LabOrderStatus.RELEASED, LabOrderStatus.CANCELLED));

        List<LabOrder> found = new ArrayList<>();
        if (!open.isEmpty()) {
            found.addAll(orders.findByStatusInOrderByOrderedAtAsc(open).stream()
                    .sorted(WORKLIST_ORDER)
                    .toList());
        }
        if (!done.isEmpty()) {
            found.addAll(orders.findByStatusInAndOrderedAtAfterOrderByOrderedAtDesc(
                    done, Instant.now(clock).minus(Duration.ofDays(1))));
        }
        return found.stream().map(mapper::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public LabOrderResponse get(Long id) {
        return response(find(id));
    }

    @Transactional(readOnly = true)
    public List<LabOrderResponse> forVisit(Long visitId) {
        return orders.findByVisitIdOrderByOrderedAtDesc(visitId).stream()
                .map(this::response)
                .toList();
    }

    /** Previous released results for the same patient, for trend comparison. */
    @Transactional(readOnly = true)
    public List<LabOrderResponse> forPatient(Long patientId) {
        return orders.findTop50ByPatientIdOrderByOrderedAtDesc(patientId).stream()
                .filter(o -> o.getStatus() == LabOrderStatus.RELEASED)
                .map(this::response)
                .toList();
    }

    // ---------------------------------------------------------------- workflow

    @Transactional
    public LabOrderResponse collectSample(Long id) {
        LabOrder order = find(id);
        move(order, LabOrderStatus.SAMPLE_COLLECTED);
        order.setSampleCollectedAt(Instant.now(clock));
        order.setSampleCollectedBy(CurrentUser.username());
        return response(order);
    }

    /** Saves typed values (draft). Can be called repeatedly until release. */
    @Transactional
    public LabOrderResponse saveResults(Long id, ResultEntryRequest request) {
        LabOrder order = find(id);
        switch (order.getStatus()) {
            case ORDERED -> throw new BusinessRuleException("Mark the sample as collected before entering results.");
            case RELEASED -> throw new BusinessRuleException("This report is released. Use amend to correct a result.");
            case CANCELLED -> throw new BusinessRuleException("This order was cancelled.");
            default -> { }
        }
        Map<Long, LabResult> byId = order.allResults().stream()
                .collect(Collectors.toMap(LabResult::getId, Function.identity()));
        String user = CurrentUser.username();
        Instant now = Instant.now(clock);
        for (ResultEntryRequest.Entry entry : request.results()) {
            LabResult result = byId.get(entry.resultId());
            if (result == null) {
                throw new IllegalArgumentException(
                        "Result " + entry.resultId() + " does not belong to order " + order.getOrderNumber() + ".");
            }
            String before = result.displayValue();
            applyValue(result, entry.value());
            if (!Objects.equals(before, result.displayValue())) {
                result.setEnteredBy(user);
                result.setEnteredAt(now);
            }
        }
        if (order.getStatus() == LabOrderStatus.SAMPLE_COLLECTED) {
            move(order, LabOrderStatus.IN_PROGRESS);
        }
        return response(order);
    }

    /**
     * Releases the report to the doctor. Every numeric and text parameter needs a value, every test needs
     * at least one value, and critical values need the name of the clinician who was informed.
     */
    @Transactional
    public LabOrderResponse release(Long id, ReleaseRequest request) {
        LabOrder order = find(id);
        if (order.getStatus() != LabOrderStatus.IN_PROGRESS) {
            throw new BusinessRuleException("Only reports with entered results can be released (current status: " + order.getStatus() + ").");
        }
        List<String> missing = order.allResults().stream()
                .filter(r -> r.getValueType() != ValueType.NARRATIVE && !r.hasValue())
                .map(LabResult::getParameterName)
                .toList();
        if (!missing.isEmpty()) {
            throw new BusinessRuleException("Enter results for: " + String.join(", ", missing) + ".");
        }
        List<String> emptyTests = order.getItems().stream()
                .filter(i -> i.getResults().stream().noneMatch(LabResult::hasValue))
                .map(LabOrderItem::getTestName)
                .toList();
        if (!emptyTests.isEmpty()) {
            throw new BusinessRuleException("No result entered for: " + String.join(", ", emptyTests) + ".");
        }
        List<LabResult> critical = order.allResults().stream()
                .filter(r -> r.getFlag() != null && r.getFlag().isCritical())
                .toList();
        String notified = request == null ? null : blankToNull(request.criticalNotifiedTo());
        if (!critical.isEmpty() && notified == null) {
            throw new BusinessRuleException("Critical values (" + critical.stream()
                    .map(r -> r.getParameterName() + " " + r.displayValue())
                    .collect(Collectors.joining(", "))
                    + "). Phone the doctor and record who was informed before releasing.");
        }

        StaffPrincipal tech = CurrentUser.get();
        order.setReleasedAt(Instant.now(clock));
        order.setReleasedBy(tech.username());
        order.setReleasedByName(tech.fullName());
        order.setCriticalNotifiedTo(notified);
        move(order, LabOrderStatus.RELEASED);

        // Notify OPD only once the release is committed safely to the database
        Long visitId = order.getVisitId();
        Long orderId = order.getId();
        String orderNumber = order.getOrderNumber();
        boolean hasCritical = !critical.isEmpty();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    opdClient.notifyResultsReady(visitId, orderId, orderNumber, hasCritical);
                } catch (Exception e) {
                    log.error("OPD notification failed for order {}: {}", orderNumber, e.getMessage(), e);
                }
            }
        });
        return response(order);
    }


    /** Corrects a released result. The previous value, new value and reason are kept in result_amendments. */
    @Transactional
    public LabOrderResponse amend(Long id, Long resultId, AmendRequest request) {
        LabOrder order = find(id);
        if (order.getStatus() != LabOrderStatus.RELEASED) {
            throw new BusinessRuleException("Only released results are amended. Edit the draft results instead.");
        }
        LabResult result = order.allResults().stream()
                .filter(r -> r.getId().equals(resultId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Result " + resultId + " not found in order " + order.getOrderNumber() + "."));
        String previousValue = result.displayValue();
        ResultFlag previousFlag = result.getFlag();
        applyValue(result, request.value());
        if (Objects.equals(previousValue, result.displayValue())) {
            throw new BusinessRuleException("The new value is the same as the current one.");
        }
        if (result.getValueType() != ValueType.NARRATIVE && !result.hasValue()) {
            throw new BusinessRuleException("A released result cannot be blanked. Enter the corrected value.");
        }
        String user = CurrentUser.username();
        Instant now = Instant.now(clock);
        result.setEnteredBy(user);
        result.setEnteredAt(now);
        order.setAmended(true);
        amendments.save(new ResultAmendment(result, previousValue, result.displayValue(),
                previousFlag, result.getFlag(), request.reason().trim(), user, now));
        return response(order);
    }

    @Transactional
    public LabOrderResponse cancel(Long id, CancelRequest request) {
        LabOrder order = find(id);
        if (!order.getStatus().canMoveTo(LabOrderStatus.CANCELLED)) {
            throw new BusinessRuleException(
                    "A " + order.getStatus().name().toLowerCase() + " order cannot be cancelled.");
        }
        order.setCancelReason(request.reason().trim());
        move(order, LabOrderStatus.CANCELLED);
        return response(order);
    }

    // ---------------------------------------------------------------- helpers

    LabOrder find(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new NotFoundException("Lab order " + id + " not found."));
    }

    /** Doctors only see values once released; lab technicians always see them. */
    LabOrderResponse response(LabOrder order) {
        boolean visible = order.getStatus() == LabOrderStatus.RELEASED || CurrentUser.hasRole("LAB_TECH");
        Map<Long, List<ResultAmendment>> byResult = amendmentsFor(order);
        List<LabAttachment> files = order.getId() == null
                ? List.of()
                : attachments.findByOrderIdOrderByUploadedAtAsc(order.getId());
        return mapper.toResponse(order, files, byResult, visible);
    }

    Map<Long, List<ResultAmendment>> amendmentsFor(LabOrder order) {
        if (!order.isAmended()) {
            return Map.of();
        }
        List<Long> ids = order.allResults().stream().map(LabResult::getId).toList();
        return amendments.findByResultIdInOrderByAmendedAtAsc(ids).stream()
                .collect(Collectors.groupingBy(a -> a.getResult().getId()));
    }

    private static void move(LabOrder order, LabOrderStatus target) {
        if (!order.getStatus().canMoveTo(target)) {
            throw new BusinessRuleException("Order " + order.getOrderNumber() + " cannot move from "
                    + order.getStatus() + " to " + target + ".");
        }
        order.setStatus(target);
    }

    /** Parses and stores a typed value, then recomputes the flag. Blank clears the value. */
    static void applyValue(LabResult result, String raw) {
        String value = raw == null || raw.isBlank() ? null : raw.trim();
        if (result.getValueType() == ValueType.NUMERIC) {
            BigDecimal number = null;
            if (value != null) {
                try {
                    number = new BigDecimal(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            result.getParameterName() + " must be a number (got \"" + value + "\").");
                }
                if (number.abs().compareTo(MAX_ABS_VALUE) >= 0) {
                    throw new IllegalArgumentException(result.getParameterName() + " value is out of range.");
                }
            }
            result.setNumericValue(number);
            result.setTextValue(null);
        } else {
            result.setTextValue(value);
            result.setNumericValue(null);
        }
        ResolvedRange range = new ResolvedRange(
                result.getRefLow(), result.getRefHigh(), result.getCriticalLow(), result.getCriticalHigh());
        result.setFlag(ResultFlagEvaluator.evaluate(
                result.getValueType(), result.getNumericValue(), result.getTextValue(),
                range, TestParameter.parseNormalValues(result.getNormalValues())));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}