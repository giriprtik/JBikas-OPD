package com.hospital.opd.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    /**
     * Hospital-local clock. "Today" for tokens and day lists is decided in this zone,
     * timestamps are still stored as UTC instants.
     */
    @Bean
    public Clock clock(@Value("${app.timezone:Asia/Kathmandu}") String zone) {
        return Clock.system(ZoneId.of(zone));
    }

    /** For calls to lab-service. Short timeouts so a lab outage fails the request fast instead of hanging the UI. */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}
