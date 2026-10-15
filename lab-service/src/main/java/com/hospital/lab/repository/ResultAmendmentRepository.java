package com.hospital.lab.repository;

import com.hospital.lab.model.ResultAmendment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ResultAmendmentRepository extends JpaRepository<ResultAmendment, Long> {

    List<ResultAmendment> findByResultIdInOrderByAmendedAtAsc(Collection<Long> resultIds);
}
