package com.hospital.lab.service;

import com.hospital.lab.dto.*;
import com.hospital.lab.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Component
public class LabMapper {

    public LabOrderResponse toResponse(LabOrder o, List<LabAttachment> attachments,
                                       Map<Long, List<ResultAmendment>> amendments, boolean resultsVisible) {
        List<LabResult> all = o.allResults();
        int abnormal = resultsVisible ? (int) all.stream().filter(r -> r.getFlag() != null && r.getFlag().isAbnormal()).count() : 0;
        int critical = resultsVisible ? (int) all.stream().filter(r -> r.getFlag() != null && r.getFlag().isCritical()).count() : 0;
        List<LabItemResponse> items = o.getItems().stream()
                .map(i -> new LabItemResponse(i.getId(), i.getTestCode(), i.getTestName(), i.getCategory(), i.getSpecimen(),
                        i.getResults().stream().map(r -> toResponse(r, amendments.getOrDefault(r.getId(), List.of()), resultsVisible)).toList()))
                .toList();
        return new LabOrderResponse(o.getId(), o.getOrderNumber(), o.getVisitId(), o.getPatientId(), o.getMrn(),
                o.getPatientName(), o.getPatientGender(), o.getPatientAgeYears(), o.getOrderingDoctor(),
                o.getOrderingDoctorName(), o.getPriority(), o.getClinicalNotes(), o.getStatus(), o.getOrderedAt(),
                o.getSampleCollectedAt(), o.getSampleCollectedBy(), o.getReleasedAt(), o.getReleasedByName(),
                o.getCriticalNotifiedTo(), o.getCancelReason(), o.isAmended(), resultsVisible, abnormal, critical,
                items, attachments.stream().map(LabMapper::toResponse).toList());
    }

    public LabOrderSummary toSummary(LabOrder o) {
        List<LabResult> all = o.allResults();
        return new LabOrderSummary(o.getId(), o.getOrderNumber(), o.getVisitId(), o.getMrn(), o.getPatientName(),
                o.getPatientGender(), o.getPatientAgeYears(), o.getOrderingDoctorName(), o.getPriority(), o.getStatus(),
                o.getOrderedAt(), o.getReleasedAt(),
                o.getItems().stream().map(LabOrderItem::getTestName).toList(),
                (int) all.stream().filter(LabResult::hasValue).count(),
                all.size(),
                (int) all.stream().filter(r -> r.getFlag() != null && r.getFlag().isCritical()).count());
    }

    public static AttachmentResponse toResponse(LabAttachment a) {
        return new AttachmentResponse(a.getId(), a.getOriginalFilename(), a.getContentType(), a.getSizeBytes(),
                a.getDescription(), a.getUploadedBy(), a.getUploadedAt());
    }

    private static LabResultResponse toResponse(LabResult r, List<ResultAmendment> amendments, boolean visible) {
        return new LabResultResponse(r.getId(), r.getParameterCode(), r.getParameterName(), r.getValueType(), r.getUnit(),
                r.getDecimals(),
                visible ? r.displayValue() : null,
                visible ? r.getFlag() : null,
                referenceText(r),
                r.getRefLow(), r.getRefHigh(), r.getCriticalLow(), r.getCriticalHigh(),
                com.hospital.lab.model.TestParameter.parseNormalValues(r.getNormalValues()),
                visible ? r.getEnteredBy() : null,
                visible ? r.getEnteredAt() : null,
                visible ? amendments.stream().map(a -> new LabResultResponse.AmendmentResponse(a.getPreviousValue(),
                        a.getNewValue(), a.getPreviousFlag(), a.getNewFlag(), a.getReason(), a.getAmendedBy(),
                        a.getAmendedAt())).toList() : List.of());
    }

    /** "70 – 100", "≤ 200", "≥ 40", "Nil / Negative", or null when there is nothing to show. */
    public static String referenceText(LabResult r) {
        if (r.getValueType() == ValueType.TEXT) {
            List<String> normals = com.hospital.lab.model.TestParameter.parseNormalValues(r.getNormalValues());
            return normals.isEmpty() ? null : String.join(" / ", normals);
        }
        if (r.getValueType() != ValueType.NUMERIC) {
            return null;
        }
        String low = format(r.getRefLow(), r.getDecimals());
        String high = format(r.getRefHigh(), r.getDecimals());
        if (low != null && high != null) {
            return low + " – " + high;
        }
        if (high != null) {
            return "≤ " + high;
        }
        if (low != null) {
            return "≥ " + low;
        }
        return null;
    }

    private static String format(BigDecimal value, int decimals) {
        return value == null ? null : value.setScale(decimals, RoundingMode.HALF_UP).toPlainString();
    }
}
