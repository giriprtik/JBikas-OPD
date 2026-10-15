package com.hospital.lab.model;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

/** An orderable test in the catalog, e.g. CBC. Seeded by V2__seed_test_catalog.sql. */
@Entity
@Table(name = "test_definitions")
public class TestDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(length = 100)
    private String specimen;

    @Column(nullable = false)
    private int turnaroundHours;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int sortOrder;

    @OneToMany(mappedBy = "test", fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @BatchSize(size = 50)
    private List<TestParameter> parameters = new ArrayList<>();

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getSpecimen() { return specimen; }
    public int getTurnaroundHours() { return turnaroundHours; }
    public boolean isActive() { return active; }
    public int getSortOrder() { return sortOrder; }
    public List<TestParameter> getParameters() { return parameters; }
}
