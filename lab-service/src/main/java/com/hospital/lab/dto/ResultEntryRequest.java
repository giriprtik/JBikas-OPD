package com.hospital.lab.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Values as typed by the technician. An empty value clears the result. Numbers are parsed server-side. */
public record ResultEntryRequest(@NotEmpty List<@Valid Entry> results) {

    public record Entry(@NotNull Long resultId, @Size(max = 4000) String value) {
    }
}
