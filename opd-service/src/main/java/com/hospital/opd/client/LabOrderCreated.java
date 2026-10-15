package com.hospital.opd.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** The fields OPD needs from lab-service's order response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LabOrderCreated(Long id, String orderNumber) {
}
