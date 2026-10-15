package com.hospital.lab.model;

/** NUMERIC is flagged against ranges, TEXT against a list of normal values, NARRATIVE is free text (never flagged). */
public enum ValueType {
    NUMERIC, TEXT, NARRATIVE
}
