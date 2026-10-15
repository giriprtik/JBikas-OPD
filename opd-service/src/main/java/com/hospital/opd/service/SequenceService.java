package com.hospital.opd.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;

/** Database-backed number generators. Both are safe under concurrent registrations. */
@Service
public class SequenceService {

    private final JdbcTemplate jdbc;

    public SequenceService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Next medical record number, e.g. JB-000042. */
    public String nextMrn() {
        Long value = jdbc.queryForObject("SELECT nextval('patient_mrn_seq')", Long.class);
        return String.format("JB-%06d", value);
    }

    /** Next OPD token for the given day, starting at 1. Atomic thanks to the upsert row lock. */
    public int nextToken(LocalDate day) {
        // Passing day directly as an object lets the modern JDBC driver handle the mapping safely
        Integer value = jdbc.queryForObject("""
                INSERT INTO daily_token_counters (counter_date, last_value) 
                VALUES (?::date, 1)
                ON CONFLICT (counter_date) 
                DO UPDATE SET last_value = daily_token_counters.last_value + 1
                RETURNING last_value
                """, Integer.class, day);
        return value == null ? 1 : value;
    }
}
