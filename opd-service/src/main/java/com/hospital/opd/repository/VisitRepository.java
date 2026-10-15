package com.hospital.opd.repository;

import com.hospital.opd.model.Visit;
import com.hospital.opd.model.VisitStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    @EntityGraph(attributePaths = "patient")
    Optional<Visit> findWithPatientById(Long id);

    @EntityGraph(attributePaths = "patient")
    List<Visit> findByVisitDateOrderByTokenNumberAsc(LocalDate visitDate);

    @EntityGraph(attributePaths = "patient")
    List<Visit> findByVisitDateAndStatusInOrderByTokenNumberAsc(LocalDate visitDate, Collection<VisitStatus> statuses);

    /** Open visits from any day (a visit left open overnight must still be seen or closed). */
    @EntityGraph(attributePaths = "patient")
    List<Visit> findByStatusIn(Collection<VisitStatus> statuses);

    boolean existsByPatientIdAndStatusIn(Long patientId, Collection<VisitStatus> statuses);

    @EntityGraph(attributePaths = "patient")
    List<Visit> findByPatientIdOrderByRegisteredAtDesc(Long patientId);
}
