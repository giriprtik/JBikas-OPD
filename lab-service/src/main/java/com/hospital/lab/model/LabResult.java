package com.hospital.lab.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * One parameter's value. Created empty when the order is placed, with the reference limits that
 * apply to this patient copied in; the lab technician fills in the value and the flag is computed.
 */
@Entity
@Table(name = "lab_results")
public class LabResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private LabOrderItem item;

    @Column(nullable = false, length = 20)
    private String parameterCode;
    @Column(nullable = false, length = 150)
    private String parameterName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ValueType valueType;

    @Column(length = 30)
    private String unit;
    @Column(nullable = false)
    private int decimals;

    @Column(precision = 14, scale = 4)
    private BigDecimal numericValue;
    private String textValue;

    @Column(precision = 14, scale = 4)
    private BigDecimal refLow;
    @Column(precision = 14, scale = 4)
    private BigDecimal refHigh;
    @Column(precision = 14, scale = 4)
    private BigDecimal criticalLow;
    @Column(precision = 14, scale = 4)
    private BigDecimal criticalHigh;
    @Column(length = 255)
    private String normalValues;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private ResultFlag flag;

    @Column(length = 50)
    private String enteredBy;
    private Instant enteredAt;

    @Column(nullable = false)
    private int sortOrder;

    public boolean hasValue() {
        return numericValue != null || (textValue != null && !textValue.isBlank());
    }

    /** The value as shown on screen and in the report. */
    public String displayValue() {
        if (valueType == ValueType.NUMERIC) {
            return numericValue == null ? null : numericValue.setScale(decimals, java.math.RoundingMode.HALF_UP).toPlainString();
        }
        return textValue;
    }

    public Long getId() { return id; }
    public LabOrderItem getItem() { return item; }
    public void setItem(LabOrderItem item) { this.item = item; }
    public String getParameterCode() { return parameterCode; }
    public void setParameterCode(String parameterCode) { this.parameterCode = parameterCode; }
    public String getParameterName() { return parameterName; }
    public void setParameterName(String parameterName) { this.parameterName = parameterName; }
    public ValueType getValueType() { return valueType; }
    public void setValueType(ValueType valueType) { this.valueType = valueType; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public int getDecimals() { return decimals; }
    public void setDecimals(int decimals) { this.decimals = decimals; }
    public BigDecimal getNumericValue() { return numericValue; }
    public void setNumericValue(BigDecimal numericValue) { this.numericValue = numericValue; }
    public String getTextValue() { return textValue; }
    public void setTextValue(String textValue) { this.textValue = textValue; }
    public BigDecimal getRefLow() { return refLow; }
    public void setRefLow(BigDecimal refLow) { this.refLow = refLow; }
    public BigDecimal getRefHigh() { return refHigh; }
    public void setRefHigh(BigDecimal refHigh) { this.refHigh = refHigh; }
    public BigDecimal getCriticalLow() { return criticalLow; }
    public void setCriticalLow(BigDecimal criticalLow) { this.criticalLow = criticalLow; }
    public BigDecimal getCriticalHigh() { return criticalHigh; }
    public void setCriticalHigh(BigDecimal criticalHigh) { this.criticalHigh = criticalHigh; }
    public String getNormalValues() { return normalValues; }
    public void setNormalValues(String normalValues) { this.normalValues = normalValues; }
    public ResultFlag getFlag() { return flag; }
    public void setFlag(ResultFlag flag) { this.flag = flag; }
    public String getEnteredBy() { return enteredBy; }
    public void setEnteredBy(String enteredBy) { this.enteredBy = enteredBy; }
    public Instant getEnteredAt() { return enteredAt; }
    public void setEnteredAt(Instant enteredAt) { this.enteredAt = enteredAt; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
