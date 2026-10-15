package com.hospital.lab.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Order numbers like LAB-261014-00042 (date in hospital time + global sequence). Printed as the sample label. */
@Service
public class OrderNumberService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyMMdd");

    private final JdbcTemplate jdbc;
    private final Clock clock;

    public OrderNumberService(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    public String next() {
        Long seq = jdbc.queryForObject("SELECT nextval('lab_order_number_seq')", Long.class);
        return String.format("LAB-%s-%05d", LocalDate.now(clock).format(DAY), seq);
    }
}
