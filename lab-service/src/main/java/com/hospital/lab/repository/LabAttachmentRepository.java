package com.hospital.lab.repository;

import com.hospital.lab.model.LabAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabAttachmentRepository extends JpaRepository<LabAttachment, Long> {

    List<LabAttachment> findByOrderIdOrderByUploadedAtAsc(Long orderId);

    Optional<LabAttachment> findByIdAndOrderId(Long id, Long orderId);
}
