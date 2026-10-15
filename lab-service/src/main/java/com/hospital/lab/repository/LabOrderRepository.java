package com.hospital.lab.repository;

import com.hospital.lab.model.LabOrder;
import com.hospital.lab.model.LabOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {

    List<LabOrder> findByStatusInOrderByOrderedAtAsc(Collection<LabOrderStatus> statuses);

    /** Recently released / cancelled orders for the worklist's "done" tab. */
    List<LabOrder> findByStatusInAndOrderedAtAfterOrderByOrderedAtDesc(Collection<LabOrderStatus> statuses, Instant after);

    List<LabOrder> findByVisitIdOrderByOrderedAtDesc(Long visitId);

    List<LabOrder> findTop50ByPatientIdOrderByOrderedAtDesc(Long patientId);
}
