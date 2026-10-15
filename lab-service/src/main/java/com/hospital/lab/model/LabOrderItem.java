package com.hospital.lab.model;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

/** One test within an order (e.g. CBC). Test name and category are snapshots of the catalog. */
@Entity
@Table(name = "lab_order_items")
public class LabOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private LabOrder order;

    @Column(nullable = false, length = 20)
    private String testCode;
    @Column(nullable = false, length = 150)
    private String testName;
    @Column(nullable = false, length = 30)
    private String category;
    @Column(length = 100)
    private String specimen;
    @Column(nullable = false)
    private int sortOrder;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @BatchSize(size = 100)
    private List<LabResult> results = new ArrayList<>();

    public void addResult(LabResult result) {
        result.setItem(this);
        results.add(result);
    }

    public Long getId() { return id; }
    public LabOrder getOrder() { return order; }
    public void setOrder(LabOrder order) { this.order = order; }
    public String getTestCode() { return testCode; }
    public void setTestCode(String testCode) { this.testCode = testCode; }
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSpecimen() { return specimen; }
    public void setSpecimen(String specimen) { this.specimen = specimen; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public List<LabResult> getResults() { return results; }
}
