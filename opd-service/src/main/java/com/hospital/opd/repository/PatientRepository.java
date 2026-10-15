package com.hospital.opd.repository;

import com.hospital.opd.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByMrnIgnoreCase(String mrn);

    List<Patient> findTop20ByPhoneContainingOrderByFullNameAsc(String phone);

    List<Patient> findTop20ByFullNameContainingIgnoreCaseOrderByFullNameAsc(String name);
}
