package com.hospital.lab.repository;

import com.hospital.lab.model.TestDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TestDefinitionRepository extends JpaRepository<TestDefinition, Long> {

    List<TestDefinition> findByActiveTrueOrderBySortOrderAsc();

    List<TestDefinition> findByCodeIn(Collection<String> codes);
}
