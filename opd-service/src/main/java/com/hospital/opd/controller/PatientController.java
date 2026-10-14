package com.hospital.opd.controller;

import com.hospital.opd.model.Patient;
import com.hospital.opd.repository.PatientRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/opd/patients")
public class PatientController {

    private final PatientRepository patientRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public PatientController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    // 🩺 View all active waiting queue patients
    @GetMapping("/queue")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<List<Patient>> getActiveQueue() {
        return ResponseEntity.ok(patientRepository.findByStatus("QUEUED"));
    }

    // 📋 View a master history list of all patients
    @GetMapping("/all")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<List<Patient>> getAllRegisteredPatients() {
        return ResponseEntity.ok(patientRepository.findAll());
    }

    // 📝 ONLY Nurses are authorized to register incoming clinic patients
    @PostMapping("/register")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<Patient> registerPatient(@RequestBody Patient patient) {
        Patient savedPatient = patientRepository.save(new Patient(
            patient.getName(),
            patient.getAge(),
            patient.getGender(),
            patient.getContactNumber(),
            patient.getChiefComplaint()
        ));
        return ResponseEntity.ok(savedPatient);
    }

    // 🚀 CROSS-SERVICE COMMUNICATION: Doctor orders a lab test for a patient
    @PutMapping("/{id}/order-lab")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> orderLaboratoryTest(
            @PathVariable Long id, 
            @RequestParam String testType,
            HttpServletRequest request) {
        
        // 1. Fetch the patient from the local OPD database
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient Not Found: " + id));

        // 2. Intercept the active user's incoming Bearer Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing active auth token.");
        }

        // 3. Update local patient record status to show they are waiting on laboratory results
        patient.setStatus("REFERRED_TO_LAB");
        patientRepository.save(patient);

        // 4. Wrap the lab payload parameters into a network data packet map
        Map<String, Object> labOrderRequest = new HashMap<>();
        labOrderRequest.put("patientId", patient.getId());
        labOrderRequest.put("patientName", patient.getName());
        labOrderRequest.put("testType", testType);

        // 5. Append the active authorization context headers to the background network stream
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(labOrderRequest, headers);

        try {
            // 6. Push the network call over to the internal lab-service cluster container
            String labServiceUrl = "http://lab-service:8083/api/lab/orders/create";
            ResponseEntity<String> response = restTemplate.postForEntity(labServiceUrl, entity, String.class);
            
            return ResponseEntity.ok("Lab order triggered successfully via microservice bridge! Response: " + response.getBody());
        } catch (Exception e) {
            // Rollback status to queued if the lab microservice falls offline
            patient.setStatus("QUEUED");
            patientRepository.save(patient);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Microservice communication failure: " + e.getMessage());
        }
    }
}
