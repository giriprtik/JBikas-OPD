-- Starter test catalog with adult/paediatric reference ranges.
--
-- CLINICAL REVIEW REQUIRED: these are common textbook ranges for the units shown. Reference
-- intervals depend on the analyser and reagents in use; the hospital pathologist must confirm
-- or replace every row before results are reported to patients.
--
-- Generated from one data table to keep codes and ranges consistent. Add tests in a new
-- migration (V3__...) rather than editing this file after it has been applied.


-- Complete Blood Count
INSERT INTO test_definitions (code, name, category, specimen, turnaround_hours, sort_order) VALUES ('CBC', 'Complete Blood Count', 'HEMATOLOGY', 'Whole blood (EDTA)', 4, 10);
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'HGB', 'Haemoglobin', 'g/dL', 'NUMERIC', 1, NULL, 10 FROM test_definitions WHERE code = 'CBC';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 1, 12, 11.5, 15.5, 7.0, 20.0 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'CBC' AND p.code = 'HGB';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'MALE', 12, NULL, 13.5, 17.5, 7.0, 20.0 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'CBC' AND p.code = 'HGB';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'FEMALE', 12, NULL, 12.0, 15.5, 7.0, 20.0 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'CBC' AND p.code = 'HGB';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'WBC', 'Total WBC count', '×10³/µL', 'NUMERIC', 1, NULL, 20 FROM test_definitions WHERE code = 'CBC';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 1, 12, 5.0, 15.0, 2.0, 30.0 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'CBC' AND p.code = 'WBC';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 12, NULL, 4.0, 11.0, 2.0, 30.0 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'CBC' AND p.code = 'WBC';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'RBC', 'RBC count', '×10⁶/µL', 'NUMERIC', 2, NULL, 30 FROM test_definitions WHERE code = 'CBC';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'MALE', 12, NULL, 4.5, 5.9, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'CBC' AND p.code = 'RBC';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'FEMALE', 12, NULL, 4.1, 5.1, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'CBC' AND p.code = 'RBC';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'HCT', 'Haematocrit (PCV)', '%', 'NUMERIC', 1, NULL, 40 FROM test_definitions WHERE code = 'CBC';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'MALE', 12, NULL, 41.0, 53.0, 20.0, 60.0 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'CBC' AND p.code = 'HCT';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'FEMALE', 12, NULL, 36.0, 46.0, 20.0, 60.0 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'CBC' AND p.code = 'HCT';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'PLT', 'Platelet count', '×10³/µL', 'NUMERIC', 0, NULL, 50 FROM test_definitions WHERE code = 'CBC';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 150, 450, 50, 1000 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'CBC' AND p.code = 'PLT';

-- Fasting Blood Glucose
INSERT INTO test_definitions (code, name, category, specimen, turnaround_hours, sort_order) VALUES ('FBS', 'Fasting Blood Glucose', 'BIOCHEMISTRY', 'Plasma (fluoride)', 2, 20);
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'GLU_F', 'Glucose, fasting', 'mg/dL', 'NUMERIC', 0, NULL, 10 FROM test_definitions WHERE code = 'FBS';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 70, 100, 50, 400 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'FBS' AND p.code = 'GLU_F';

-- Random Blood Glucose
INSERT INTO test_definitions (code, name, category, specimen, turnaround_hours, sort_order) VALUES ('RBS', 'Random Blood Glucose', 'BIOCHEMISTRY', 'Plasma (fluoride)', 1, 30);
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'GLU_R', 'Glucose, random', 'mg/dL', 'NUMERIC', 0, NULL, 10 FROM test_definitions WHERE code = 'RBS';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 70, 140, 50, 400 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'RBS' AND p.code = 'GLU_R';

-- Glycated Haemoglobin (HbA1c)
INSERT INTO test_definitions (code, name, category, specimen, turnaround_hours, sort_order) VALUES ('HBA1C', 'Glycated Haemoglobin (HbA1c)', 'BIOCHEMISTRY', 'Whole blood (EDTA)', 24, 40);
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'HBA1C', 'HbA1c', '%', 'NUMERIC', 1, NULL, 10 FROM test_definitions WHERE code = 'HBA1C';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 4.0, 5.6, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'HBA1C' AND p.code = 'HBA1C';

-- Renal Function Test
INSERT INTO test_definitions (code, name, category, specimen, turnaround_hours, sort_order) VALUES ('RFT', 'Renal Function Test', 'BIOCHEMISTRY', 'Serum', 4, 50);
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'UREA', 'Urea', 'mg/dL', 'NUMERIC', 0, NULL, 10 FROM test_definitions WHERE code = 'RFT';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 15, 40, NULL, 200 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'RFT' AND p.code = 'UREA';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'CREAT', 'Creatinine', 'mg/dL', 'NUMERIC', 2, NULL, 20 FROM test_definitions WHERE code = 'RFT';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'MALE', 12, NULL, 0.7, 1.3, NULL, 10.0 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'RFT' AND p.code = 'CREAT';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'FEMALE', 12, NULL, 0.6, 1.1, NULL, 10.0 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'RFT' AND p.code = 'CREAT';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'NA', 'Sodium', 'mmol/L', 'NUMERIC', 0, NULL, 30 FROM test_definitions WHERE code = 'RFT';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 135, 145, 120, 160 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'RFT' AND p.code = 'NA';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'K', 'Potassium', 'mmol/L', 'NUMERIC', 1, NULL, 40 FROM test_definitions WHERE code = 'RFT';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 3.5, 5.1, 2.5, 6.5 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'RFT' AND p.code = 'K';

-- Liver Function Test
INSERT INTO test_definitions (code, name, category, specimen, turnaround_hours, sort_order) VALUES ('LFT', 'Liver Function Test', 'BIOCHEMISTRY', 'Serum', 4, 60);
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'TBIL', 'Bilirubin, total', 'mg/dL', 'NUMERIC', 1, NULL, 10 FROM test_definitions WHERE code = 'LFT';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 0.1, 1.2, NULL, 15.0 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'LFT' AND p.code = 'TBIL';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'DBIL', 'Bilirubin, direct', 'mg/dL', 'NUMERIC', 1, NULL, 20 FROM test_definitions WHERE code = 'LFT';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 0.0, 0.3, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'LFT' AND p.code = 'DBIL';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'ALT', 'ALT (SGPT)', 'U/L', 'NUMERIC', 0, NULL, 30 FROM test_definitions WHERE code = 'LFT';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 7, 56, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'LFT' AND p.code = 'ALT';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'AST', 'AST (SGOT)', 'U/L', 'NUMERIC', 0, NULL, 40 FROM test_definitions WHERE code = 'LFT';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 10, 40, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'LFT' AND p.code = 'AST';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'ALP', 'Alkaline phosphatase', 'U/L', 'NUMERIC', 0, NULL, 50 FROM test_definitions WHERE code = 'LFT';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 44, 147, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'LFT' AND p.code = 'ALP';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'ALB', 'Albumin', 'g/dL', 'NUMERIC', 1, NULL, 60 FROM test_definitions WHERE code = 'LFT';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 3.5, 5.0, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'LFT' AND p.code = 'ALB';

-- Lipid Profile
INSERT INTO test_definitions (code, name, category, specimen, turnaround_hours, sort_order) VALUES ('LIPID', 'Lipid Profile', 'BIOCHEMISTRY', 'Serum (fasting)', 6, 70);
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'TCHOL', 'Total cholesterol', 'mg/dL', 'NUMERIC', 0, NULL, 10 FROM test_definitions WHERE code = 'LIPID';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, NULL, 200, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'LIPID' AND p.code = 'TCHOL';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'TG', 'Triglycerides', 'mg/dL', 'NUMERIC', 0, NULL, 20 FROM test_definitions WHERE code = 'LIPID';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, NULL, 150, NULL, 1000 FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'LIPID' AND p.code = 'TG';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'HDL', 'HDL cholesterol', 'mg/dL', 'NUMERIC', 0, NULL, 30 FROM test_definitions WHERE code = 'LIPID';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'MALE', 0, NULL, 40, NULL, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'LIPID' AND p.code = 'HDL';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'FEMALE', 0, NULL, 50, NULL, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'LIPID' AND p.code = 'HDL';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'LDL', 'LDL cholesterol', 'mg/dL', 'NUMERIC', 0, NULL, 40 FROM test_definitions WHERE code = 'LIPID';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, NULL, 100, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'LIPID' AND p.code = 'LDL';

-- Thyroid Function Test
INSERT INTO test_definitions (code, name, category, specimen, turnaround_hours, sort_order) VALUES ('TFT', 'Thyroid Function Test', 'ENDOCRINOLOGY', 'Serum', 24, 80);
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'TSH', 'TSH', 'mIU/L', 'NUMERIC', 2, NULL, 10 FROM test_definitions WHERE code = 'TFT';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 0.4, 4.0, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'TFT' AND p.code = 'TSH';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'FT4', 'Free T4', 'ng/dL', 'NUMERIC', 2, NULL, 20 FROM test_definitions WHERE code = 'TFT';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 0.8, 1.8, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'TFT' AND p.code = 'FT4';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'FT3', 'Free T3', 'pg/mL', 'NUMERIC', 2, NULL, 30 FROM test_definitions WHERE code = 'TFT';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 2.3, 4.2, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'TFT' AND p.code = 'FT3';

-- Urine Routine Examination
INSERT INTO test_definitions (code, name, category, specimen, turnaround_hours, sort_order) VALUES ('URINE_RE', 'Urine Routine Examination', 'URINE', 'Urine (midstream)', 2, 90);
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'U_COLOR', 'Colour', NULL, 'TEXT', 0, 'Pale yellow,Yellow,Straw', 10 FROM test_definitions WHERE code = 'URINE_RE';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'U_APPEAR', 'Appearance', NULL, 'TEXT', 0, 'Clear', 20 FROM test_definitions WHERE code = 'URINE_RE';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'U_PH', 'pH', NULL, 'NUMERIC', 1, NULL, 30 FROM test_definitions WHERE code = 'URINE_RE';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 4.5, 8.0, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'URINE_RE' AND p.code = 'U_PH';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'U_SG', 'Specific gravity', NULL, 'NUMERIC', 3, NULL, 40 FROM test_definitions WHERE code = 'URINE_RE';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 1.005, 1.03, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'URINE_RE' AND p.code = 'U_SG';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'U_PROT', 'Protein', NULL, 'TEXT', 0, 'Nil,Negative', 50 FROM test_definitions WHERE code = 'URINE_RE';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'U_GLU', 'Glucose', NULL, 'TEXT', 0, 'Nil,Negative', 60 FROM test_definitions WHERE code = 'URINE_RE';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'U_KET', 'Ketones', NULL, 'TEXT', 0, 'Nil,Negative', 70 FROM test_definitions WHERE code = 'URINE_RE';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'U_PUS', 'Pus cells', '/HPF', 'NUMERIC', 0, NULL, 80 FROM test_definitions WHERE code = 'URINE_RE';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 0, 5, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'URINE_RE' AND p.code = 'U_PUS';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'U_RBC', 'Red blood cells', '/HPF', 'NUMERIC', 0, NULL, 90 FROM test_definitions WHERE code = 'URINE_RE';
INSERT INTO reference_ranges (parameter_id, gender, age_min_years, age_max_years, low, high, critical_low, critical_high) SELECT p.id, 'ANY', 0, NULL, 0, 2, NULL, NULL FROM test_parameters p JOIN test_definitions t ON t.id = p.test_id WHERE t.code = 'URINE_RE' AND p.code = 'U_RBC';

-- Chest X-Ray (PA view)
INSERT INTO test_definitions (code, name, category, specimen, turnaround_hours, sort_order) VALUES ('CXR', 'Chest X-Ray (PA view)', 'RADIOLOGY', 'Imaging', 24, 100);
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'CXR_FIND', 'Findings', NULL, 'NARRATIVE', 0, NULL, 10 FROM test_definitions WHERE code = 'CXR';
INSERT INTO test_parameters (test_id, code, name, unit, value_type, decimals, normal_values, sort_order) SELECT id, 'CXR_IMP', 'Impression', NULL, 'NARRATIVE', 0, NULL, 20 FROM test_definitions WHERE code = 'CXR';
