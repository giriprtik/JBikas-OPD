-- Lab service schema. Flyway owns this database (Hibernate ddl-auto=none).
-- Never edit an applied migration; add a new V<n>__ file instead.

-- ============================================================ catalog
CREATE TABLE test_definitions (
    id               BIGSERIAL PRIMARY KEY,
    code             VARCHAR(20)  NOT NULL UNIQUE,
    name             VARCHAR(150) NOT NULL,
    category         VARCHAR(30)  NOT NULL,
    specimen         VARCHAR(100),
    turnaround_hours INTEGER      NOT NULL DEFAULT 24,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    sort_order       INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE test_parameters (
    id            BIGSERIAL PRIMARY KEY,
    test_id       BIGINT       NOT NULL REFERENCES test_definitions (id),
    code          VARCHAR(20)  NOT NULL,
    name          VARCHAR(150) NOT NULL,
    unit          VARCHAR(30),
    value_type    VARCHAR(10)  NOT NULL CHECK (value_type IN ('NUMERIC', 'TEXT', 'NARRATIVE')),
    decimals      INTEGER      NOT NULL DEFAULT 1,
    -- TEXT parameters: comma-separated values that count as normal, e.g. 'Nil,Negative'
    normal_values VARCHAR(255),
    sort_order    INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT uq_test_parameters_code UNIQUE (test_id, code)
);

-- Reference ranges by sex and age band. age_max_years is exclusive; NULL means no upper age limit.
CREATE TABLE reference_ranges (
    id            BIGSERIAL PRIMARY KEY,
    parameter_id  BIGINT        NOT NULL REFERENCES test_parameters (id),
    gender        VARCHAR(10)   NOT NULL DEFAULT 'ANY' CHECK (gender IN ('ANY', 'MALE', 'FEMALE')),
    age_min_years INTEGER       NOT NULL DEFAULT 0,
    age_max_years INTEGER,
    low           NUMERIC(14, 4),
    high          NUMERIC(14, 4),
    critical_low  NUMERIC(14, 4),
    critical_high NUMERIC(14, 4)
);
CREATE INDEX idx_reference_ranges_parameter ON reference_ranges (parameter_id);

-- ============================================================ orders
CREATE SEQUENCE lab_order_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE lab_orders (
    id                   BIGSERIAL PRIMARY KEY,
    order_number         VARCHAR(30)  NOT NULL UNIQUE,
    visit_id             BIGINT       NOT NULL,
    patient_id           BIGINT       NOT NULL,
    mrn                  VARCHAR(20)  NOT NULL,
    patient_name         VARCHAR(150) NOT NULL,
    patient_gender       VARCHAR(10)  NOT NULL,
    patient_age_years    INTEGER      NOT NULL,
    ordering_doctor      VARCHAR(50)  NOT NULL,
    ordering_doctor_name VARCHAR(150),
    priority             VARCHAR(10)  NOT NULL CHECK (priority IN ('ROUTINE', 'URGENT', 'STAT')),
    clinical_notes       TEXT,
    status               VARCHAR(20)  NOT NULL CHECK (status IN ('ORDERED', 'SAMPLE_COLLECTED', 'IN_PROGRESS',
                                                                  'RELEASED', 'CANCELLED')),
    ordered_at           TIMESTAMPTZ  NOT NULL,
    sample_collected_at  TIMESTAMPTZ,
    sample_collected_by  VARCHAR(50),
    released_at          TIMESTAMPTZ,
    released_by          VARCHAR(50),
    released_by_name     VARCHAR(150),
    critical_notified_to VARCHAR(150),
    cancel_reason        TEXT,
    amended              BOOLEAN      NOT NULL DEFAULT FALSE,
    version              BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_lab_orders_status ON lab_orders (status, ordered_at);
CREATE INDEX idx_lab_orders_visit ON lab_orders (visit_id);
CREATE INDEX idx_lab_orders_patient ON lab_orders (patient_id);

CREATE TABLE lab_order_items (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT       NOT NULL REFERENCES lab_orders (id),
    test_code  VARCHAR(20)  NOT NULL,
    test_name  VARCHAR(150) NOT NULL,
    category   VARCHAR(30)  NOT NULL,
    specimen   VARCHAR(100),
    sort_order INTEGER      NOT NULL DEFAULT 0
);
CREATE INDEX idx_lab_order_items_order ON lab_order_items (order_id);

-- One row per parameter. Reference limits are copied from the catalog when the order is placed,
-- so later catalog edits never change the interpretation of an existing report.
CREATE TABLE lab_results (
    id             BIGSERIAL PRIMARY KEY,
    item_id        BIGINT       NOT NULL REFERENCES lab_order_items (id),
    parameter_code VARCHAR(20)  NOT NULL,
    parameter_name VARCHAR(150) NOT NULL,
    value_type     VARCHAR(10)  NOT NULL,
    unit           VARCHAR(30),
    decimals       INTEGER      NOT NULL DEFAULT 1,
    numeric_value  NUMERIC(14, 4),
    text_value     TEXT,
    ref_low        NUMERIC(14, 4),
    ref_high       NUMERIC(14, 4),
    critical_low   NUMERIC(14, 4),
    critical_high  NUMERIC(14, 4),
    normal_values  VARCHAR(255),
    flag           VARCHAR(15),
    entered_by     VARCHAR(50),
    entered_at     TIMESTAMPTZ,
    sort_order     INTEGER      NOT NULL DEFAULT 0
);
CREATE INDEX idx_lab_results_item ON lab_results (item_id);

-- Changes to a released result. Append-only.
CREATE TABLE result_amendments (
    id             BIGSERIAL PRIMARY KEY,
    result_id      BIGINT      NOT NULL REFERENCES lab_results (id),
    previous_value TEXT,
    new_value      TEXT,
    previous_flag  VARCHAR(15),
    new_flag       VARCHAR(15),
    reason         TEXT        NOT NULL,
    amended_by     VARCHAR(50) NOT NULL,
    amended_at     TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_result_amendments_result ON result_amendments (result_id);

-- Uploaded files (scanned reports, analyser printouts, X-ray images). Bytes live on disk; storage_key is a UUID.
CREATE TABLE lab_attachments (
    id                BIGSERIAL PRIMARY KEY,
    order_id          BIGINT       NOT NULL REFERENCES lab_orders (id),
    original_filename VARCHAR(255) NOT NULL,
    content_type      VARCHAR(100) NOT NULL,
    size_bytes        BIGINT       NOT NULL,
    storage_key       VARCHAR(64)  NOT NULL UNIQUE,
    description       VARCHAR(255),
    uploaded_by       VARCHAR(50)  NOT NULL,
    uploaded_at       TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_lab_attachments_order ON lab_attachments (order_id);
