package com.hospital.opd.repository;

import com.hospital.opd.model.VitalSigns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface VitalSignsRepository extends JpaRepository<VitalSigns, Long> {

    List<VitalSigns> findByVisitIdOrderByRecordedAtDesc(Long visitId);

    /** For queue screens: all vitals of the listed visits, newest first, so the first per visit is the latest. */
    List<VitalSigns> findByVisitIdInOrderByRecordedAtDesc(Collection<Long> visitIds);
}
