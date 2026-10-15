"""Generates lab-service V2__seed_test_catalog.sql from one data table so codes, units and ranges stay consistent.

Usage: python3 tools/gen_lab_catalog.py lab-service/src/main/resources/db/migration/V2__seed_test_catalog.sql
Only regenerate before the migration has been applied anywhere; afterwards add a new V<n> migration.
"""
import sys

# Each range: (gender, age_min, age_max, low, high, crit_low, crit_high)
A = 'ANY'; M = 'MALE'; F = 'FEMALE'; N = None

CATALOG = [
    # code, name, category, specimen, tat_hours, params
    ('CBC', 'Complete Blood Count', 'HEMATOLOGY', 'Whole blood (EDTA)', 4, [
        # code, name, unit, type, decimals, normal_values, ranges
        ('HGB', 'Haemoglobin', 'g/dL', 'NUMERIC', 1, N, [
            (A, 1, 12, 11.5, 15.5, 7.0, 20.0),
            (M, 12, N, 13.5, 17.5, 7.0, 20.0),
            (F, 12, N, 12.0, 15.5, 7.0, 20.0)]),
        ('WBC', 'Total WBC count', '×10³/µL', 'NUMERIC', 1, N, [
            (A, 1, 12, 5.0, 15.0, 2.0, 30.0),
            (A, 12, N, 4.0, 11.0, 2.0, 30.0)]),
        ('RBC', 'RBC count', '×10⁶/µL', 'NUMERIC', 2, N, [
            (M, 12, N, 4.5, 5.9, N, N),
            (F, 12, N, 4.1, 5.1, N, N)]),
        ('HCT', 'Haematocrit (PCV)', '%', 'NUMERIC', 1, N, [
            (M, 12, N, 41.0, 53.0, 20.0, 60.0),
            (F, 12, N, 36.0, 46.0, 20.0, 60.0)]),
        ('PLT', 'Platelet count', '×10³/µL', 'NUMERIC', 0, N, [
            (A, 0, N, 150, 450, 50, 1000)]),
    ]),
    ('FBS', 'Fasting Blood Glucose', 'BIOCHEMISTRY', 'Plasma (fluoride)', 2, [
        ('GLU_F', 'Glucose, fasting', 'mg/dL', 'NUMERIC', 0, N, [
            (A, 0, N, 70, 100, 50, 400)]),
    ]),
    ('RBS', 'Random Blood Glucose', 'BIOCHEMISTRY', 'Plasma (fluoride)', 1, [
        ('GLU_R', 'Glucose, random', 'mg/dL', 'NUMERIC', 0, N, [
            (A, 0, N, 70, 140, 50, 400)]),
    ]),
    ('HBA1C', 'Glycated Haemoglobin (HbA1c)', 'BIOCHEMISTRY', 'Whole blood (EDTA)', 24, [
        ('HBA1C', 'HbA1c', '%', 'NUMERIC', 1, N, [
            (A, 0, N, 4.0, 5.6, N, N)]),
    ]),
    ('RFT', 'Renal Function Test', 'BIOCHEMISTRY', 'Serum', 4, [
        ('UREA', 'Urea', 'mg/dL', 'NUMERIC', 0, N, [(A, 0, N, 15, 40, N, 200)]),
        ('CREAT', 'Creatinine', 'mg/dL', 'NUMERIC', 2, N, [
            (M, 12, N, 0.7, 1.3, N, 10.0),
            (F, 12, N, 0.6, 1.1, N, 10.0)]),
        ('NA', 'Sodium', 'mmol/L', 'NUMERIC', 0, N, [(A, 0, N, 135, 145, 120, 160)]),
        ('K', 'Potassium', 'mmol/L', 'NUMERIC', 1, N, [(A, 0, N, 3.5, 5.1, 2.5, 6.5)]),
    ]),
    ('LFT', 'Liver Function Test', 'BIOCHEMISTRY', 'Serum', 4, [
        ('TBIL', 'Bilirubin, total', 'mg/dL', 'NUMERIC', 1, N, [(A, 0, N, 0.1, 1.2, N, 15.0)]),
        ('DBIL', 'Bilirubin, direct', 'mg/dL', 'NUMERIC', 1, N, [(A, 0, N, 0.0, 0.3, N, N)]),
        ('ALT', 'ALT (SGPT)', 'U/L', 'NUMERIC', 0, N, [(A, 0, N, 7, 56, N, N)]),
        ('AST', 'AST (SGOT)', 'U/L', 'NUMERIC', 0, N, [(A, 0, N, 10, 40, N, N)]),
        ('ALP', 'Alkaline phosphatase', 'U/L', 'NUMERIC', 0, N, [(A, 0, N, 44, 147, N, N)]),
        ('ALB', 'Albumin', 'g/dL', 'NUMERIC', 1, N, [(A, 0, N, 3.5, 5.0, N, N)]),
    ]),
    ('LIPID', 'Lipid Profile', 'BIOCHEMISTRY', 'Serum (fasting)', 6, [
        ('TCHOL', 'Total cholesterol', 'mg/dL', 'NUMERIC', 0, N, [(A, 0, N, N, 200, N, N)]),
        ('TG', 'Triglycerides', 'mg/dL', 'NUMERIC', 0, N, [(A, 0, N, N, 150, N, 1000)]),
        ('HDL', 'HDL cholesterol', 'mg/dL', 'NUMERIC', 0, N, [
            (M, 0, N, 40, N, N, N),
            (F, 0, N, 50, N, N, N)]),
        ('LDL', 'LDL cholesterol', 'mg/dL', 'NUMERIC', 0, N, [(A, 0, N, N, 100, N, N)]),
    ]),
    ('TFT', 'Thyroid Function Test', 'ENDOCRINOLOGY', 'Serum', 24, [
        ('TSH', 'TSH', 'mIU/L', 'NUMERIC', 2, N, [(A, 0, N, 0.4, 4.0, N, N)]),
        ('FT4', 'Free T4', 'ng/dL', 'NUMERIC', 2, N, [(A, 0, N, 0.8, 1.8, N, N)]),
        ('FT3', 'Free T3', 'pg/mL', 'NUMERIC', 2, N, [(A, 0, N, 2.3, 4.2, N, N)]),
    ]),
    ('URINE_RE', 'Urine Routine Examination', 'URINE', 'Urine (midstream)', 2, [
        ('U_COLOR', 'Colour', N, 'TEXT', 0, 'Pale yellow,Yellow,Straw', []),
        ('U_APPEAR', 'Appearance', N, 'TEXT', 0, 'Clear', []),
        ('U_PH', 'pH', N, 'NUMERIC', 1, N, [(A, 0, N, 4.5, 8.0, N, N)]),
        ('U_SG', 'Specific gravity', N, 'NUMERIC', 3, N, [(A, 0, N, 1.005, 1.030, N, N)]),
        ('U_PROT', 'Protein', N, 'TEXT', 0, 'Nil,Negative', []),
        ('U_GLU', 'Glucose', N, 'TEXT', 0, 'Nil,Negative', []),
        ('U_KET', 'Ketones', N, 'TEXT', 0, 'Nil,Negative', []),
        ('U_PUS', 'Pus cells', '/HPF', 'NUMERIC', 0, N, [(A, 0, N, 0, 5, N, N)]),
        ('U_RBC', 'Red blood cells', '/HPF', 'NUMERIC', 0, N, [(A, 0, N, 0, 2, N, N)]),
    ]),
    ('CXR', 'Chest X-Ray (PA view)', 'RADIOLOGY', 'Imaging', 24, [
        ('CXR_FIND', 'Findings', N, 'NARRATIVE', 0, N, []),
        ('CXR_IMP', 'Impression', N, 'NARRATIVE', 0, N, []),
    ]),
]


def lit(v):
    if v is None:
        return 'NULL'
    if isinstance(v, str):
        return "'" + v.replace("'", "''") + "'"
    return repr(v)


out = ["""-- Starter test catalog with adult/paediatric reference ranges.
--
-- CLINICAL REVIEW REQUIRED: these are common textbook ranges for the units shown. Reference
-- intervals depend on the analyser and reagents in use; the hospital pathologist must confirm
-- or replace every row before results are reported to patients.
--
-- Generated from one data table to keep codes and ranges consistent. Add tests in a new
-- migration (V3__...) rather than editing this file after it has been applied.
"""]
for t_order, (code, name, cat, spec, tat, params) in enumerate(CATALOG, 1):
    out.append(f"\n-- {name}")
    out.append(f"INSERT INTO test_definitions (code, name, category, specimen, turnaround_hours, sort_order) "
               f"VALUES ({lit(code)}, {lit(name)}, {lit(cat)}, {lit(spec)}, {tat}, {t_order * 10});")
    for p_order, (pcode, pname, unit, vtype, dec, normals, ranges) in enumerate(params, 1):
        out.append(f"INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) "
                   f"SELECT id, {lit(pcode)}, {lit(pname)}, {lit(unit)}, {lit(vtype)}, {dec}, {lit(normals)}, {p_order * 10} "
                   f"FROM test_definitions WHERE code = {lit(code)};")
        for (g, amin, amax, lo, hi, clo, chi) in ranges:
            out.append(
                "INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) "
                f"SELECT p.id, {lit(g)}, {amin}, {lit(amax)}, {lit(lo)}, {lit(hi)}, {lit(clo)}, {lit(chi)} "
                f"FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id "
                f"WHERE t.code = {lit(code)} AND p.code = {lit(pcode)};")
open(sys.argv[1], 'w').write('\n'.join(out) + '\n')
print(sum(len(t[5]) for t in CATALOG), 'parameters in', len(CATALOG), 'tests')
