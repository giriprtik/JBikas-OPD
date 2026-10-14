package com.hospital.lab.controller;

import com.hospital.lab.model.LabOrder;
import com.hospital.lab.repository.LabOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lab/orders")
public class LabOrderController {

    private final LabOrderRepository labOrderRepository;

    public LabOrderController(LabOrderRepository labOrderRepository) {
        this.labOrderRepository = labOrderRepository;
    }

    // 🔬 Both Doctors and Lab Technicians/Nurses can see pending test requests
    @GetMapping("/pending")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<List<LabOrder>> getPendingOrders() {
        return ResponseEntity.ok(labOrderRepository.findByStatus("PENDING"));
    }

    // 🩺 Allows the system (or a doctor) to programmatically submit a new test request
    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<LabOrder> createLabOrder(@RequestBody LabOrder order) {
        LabOrder savedOrder = labOrderRepository.save(new LabOrder(
            order.getPatientId(),
            order.getPatientName(),
            order.getTestType()
        ));
        return ResponseEntity.ok(savedOrder);
    }

    // 🧪 ONLY Doctors or specialized staff can upload or complete finalized test results
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<LabOrder> completeLabOrder(@PathVariable Long id, @RequestBody String testResult) {
        LabOrder order = labOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lab Order Not Found: " + id));
        
        order.setTestResult(testResult);
        order.setStatus("COMPLETED");
        return ResponseEntity.ok(labOrderRepository.save(order));
    }
}
