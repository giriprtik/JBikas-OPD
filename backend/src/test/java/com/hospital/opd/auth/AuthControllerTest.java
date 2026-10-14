package com.hospital.opd.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldAuthenticateDefaultDoctorSuccessfully() throws Exception {
        String loginPayload = """
            {
                "username": "doctor1",
                "password": "doctor123"
            }
        """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("doctor1"));
    }

    @Test
    public void shouldRejectInvalidCredentials() throws Exception {
        String invalidPayload = """
            {
                "username": "doctor1",
                "password": "wrongpassword"
            }
        """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldBlockUnauthenticatedRequestsToProtectedModules() throws Exception {
        // Trying to access an arbitrary secured OPD module path without a token
        mockMvc.perform(get("/api/opd/appointments"))
                .andExpect(status().isUnauthorized());
    }
}
