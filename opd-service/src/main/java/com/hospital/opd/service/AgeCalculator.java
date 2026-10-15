package com.hospital.opd.service;

import java.time.LocalDate;
import java.time.Period;

/** Age as clinicians write it: "34 y", "18 mo" for under-2s, "12 d" for newborns. */
public final class AgeCalculator {

    private AgeCalculator() {
    }

    public static int years(LocalDate dob, LocalDate today) {
        return Math.max(0, Period.between(dob, today).getYears());
    }

    public static String display(LocalDate dob, LocalDate today) {
        Period p = Period.between(dob, today);
        if (p.isNegative()) {
            return "0 d";
        }
        if (p.getYears() >= 2) {
            return p.getYears() + " y";
        }
        int months = p.getYears() * 12 + p.getMonths();
        if (months >= 1) {
            return months + " mo";
        }
        return p.getDays() + " d";
    }
}
