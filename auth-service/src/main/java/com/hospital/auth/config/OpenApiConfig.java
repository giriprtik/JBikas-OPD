package com.hospital.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("JBikas Authentication Service API")
                .version("1.0.0")
                .description("Isolated Identity & Access Management system for hospital personnel. Generates cryptographic JWT tokens."));
    }
}
