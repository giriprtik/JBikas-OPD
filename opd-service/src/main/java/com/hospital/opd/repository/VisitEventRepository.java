package com.hospital.opd.repository;

import com.hospital.opd.model.VisitEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitEventRepository extends JpaRepository<VisitEvent, Long> {

    List<VisitEvent> findByVisitIdOrderByOccurredAtAsc(Long visitId);
}
