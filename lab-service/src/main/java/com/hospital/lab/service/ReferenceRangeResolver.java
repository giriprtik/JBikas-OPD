package com.hospital.lab.service;

import com.hospital.lab.model.ReferenceRange;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Picks the reference range for a patient's sex and age.
 *
 * <ol>
 *   <li>Only ranges whose age band contains the patient's age are considered.</li>
 *   <li>A range for the patient's own sex wins over an ANY range.</li>
 *   <li>Among equals, the narrowest age band wins (a 1–12 paediatric band beats 0–∞).</li>
 *   <li>For gender OTHER, if only sex-specific ranges exist, the union of the male and female ranges is used,
 *       so a value normal for either sex is not flagged.</li>
 * </ol>
 */
public final class ReferenceRangeResolver {

    private static final Comparator<ReferenceRange> NARROWEST_FIRST = Comparator.comparingInt(
            r -> (r.getAgeMaxYears() == null ? 200 : r.getAgeMaxYears()) - r.getAgeMinYears());

    private ReferenceRangeResolver() {
    }

    public static Optional<ResolvedRange> resolve(List<ReferenceRange> ranges, String patientGender, int ageYears) {
        String gender = patientGender == null ? "" : patientGender.trim().toUpperCase();
        List<ReferenceRange> forAge = ranges.stream()
                .filter(r -> r.getAgeMinYears() <= ageYears && (r.getAgeMaxYears() == null || ageYears < r.getAgeMaxYears()))
                .toList();

        Optional<ReferenceRange> ownSex = forAge.stream().filter(r -> r.getGender().equals(gender)).min(NARROWEST_FIRST);
        if (ownSex.isPresent()) {
            return ownSex.map(ReferenceRangeResolver::toResolved);
        }
        Optional<ReferenceRange> any = forAge.stream().filter(r -> "ANY".equals(r.getGender())).min(NARROWEST_FIRST);
        if (any.isPresent()) {
            return any.map(ReferenceRangeResolver::toResolved);
        }
        if (gender.equals("MALE") || gender.equals("FEMALE")) {
            return Optional.empty();
        }
        List<ReferenceRange> sexSpecific = forAge.stream()
                .filter(r -> r.getGender().equals("MALE") || r.getGender().equals("FEMALE"))
                .toList();
        if (sexSpecific.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ResolvedRange(
                widest(sexSpecific, ReferenceRange::getLow, BigDecimal::min),
                widest(sexSpecific, ReferenceRange::getHigh, BigDecimal::max),
                widest(sexSpecific, ReferenceRange::getCriticalLow, BigDecimal::min),
                widest(sexSpecific, ReferenceRange::getCriticalHigh, BigDecimal::max)));
    }

    /** A missing limit on any range means no limit in the union. */
    private static BigDecimal widest(List<ReferenceRange> ranges, Function<ReferenceRange, BigDecimal> limit,
                                     BinaryOperator<BigDecimal> pick) {
        List<BigDecimal> values = ranges.stream().map(limit).toList();
        if (values.stream().anyMatch(Objects::isNull)) {
            return null;
        }
        return values.stream().reduce(pick).orElse(null);
    }

    private static ResolvedRange toResolved(ReferenceRange r) {
        return new ResolvedRange(r.getLow(), r.getHigh(), r.getCriticalLow(), r.getCriticalHigh());
    }
}
