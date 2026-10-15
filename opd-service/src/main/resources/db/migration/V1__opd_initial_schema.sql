-- OPD service schema. Flyway owns this database: never edit an applied migration,
-- add V2__..., V3__... instead. Hibernate runs with ddl-auto=none.

-- Medical record numbers: JB-000001, JB-000002, ...
CREATE SEQUENCE patient_mrn_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE patients (
    id                      BIGSERIAL PRIMARY KEY,
    mrn                     VARCHAR(20)  NOT NULL UNIQUE,
    full_name               VARCHAR(150) NOT NULL,
    date_of_birth           DATE         NOT NULL,
    -- TRUE when only an age was given at registration and the DOB was derived from it
    dob_estimated           BOOLEAN      NOT NULL DEFAULT FALSE,
    gender                  VARCHAR(10)  NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    phone                   VARCHAR(20)  NOT NULL,
    address                 VARCHAR(255),
    blood_group             VARCHAR(5),
    allergies               TEXT,
    emergency_contact_name  VARCHAR(150),
    emergency_contact_phone VARCHAR(20),
    created_at              TIMESTAMPTZ  NOT NULL,
    created_by              VARCHAR(50)  NOT NULL,
    updated_at              TIMESTAMPTZ,
    version                 BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_patients_phone ON patients (phone);
CREATE INDEX idx_patients_name_lower ON patients (lower(full_name));

CREATE TABLE visits (
    id                      BIGSERIAL PRIMARY KEY,
    patient_id              BIGINT      NOT NULL REFERENCES patients (id),
    visit_date              DATE        NOT NULL,
    token_number            INTEGER     NOT NULL,
    chief_complaint         TEXT        NOT NULL,
    priority                VARCHAR(15) NOT NULL CHECK (priority IN ('NORMAL', 'URGENT', 'EMERGENCY')),
    status                  VARCHAR(25) NOT NULL CHECK (status IN ('REGISTERED', 'TRIAGED', 'IN_CONSULTATION',
                                                    'AWAITING_LAB', 'LAB_RESULTS_READY', 'COMPLETED', 'CANCELLED')),
    assigned_doctor         VARCHAR(50),
    assigned_doctor_name    VARCHAR(150),
    consultation_notes      TEXT,
    diagnosis               TEXT,
    latest_lab_order_id     BIGINT,
    cancel_reason           TEXT,
    registered_at           TIMESTAMPTZ NOT NULL,
    registered_by           VARCHAR(50) NOT NULL,
    consultation_started_at TIMESTAMPTZ,
    completed_at            TIMESTAMPTZ,
    version                 BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uq_visits_token_per_day UNIQUE (visit_date, token_number)
);
CREATE INDEX idx_visits_date_status ON visits (visit_date, status);
CREATE INDEX idx_visits_patient ON visits (patient_id);
-- A patient can have only one open visit at a time.
CREATE UNIQUE INDEX uq_visits_one_active_per_patient ON visits (patient_id)
    WHERE status NOT IN ('COMPLETED', 'CANCELLED');

CREATE TABLE vital_signs (
    id               BIGSERIAL PRIMARY KEY,
    visit_id         BIGINT       NOT NULL REFERENCES visits (id),
    temperature_c    NUMERIC(4, 1),
    pulse_bpm        INTEGER,
    respiratory_rate INTEGER,
    systolic_bp      INTEGER,
    diastolic_bp     INTEGER,
    spo2             INTEGER,
    weight_kg        NUMERIC(5, 1),
    height_cm        NUMERIC(5, 1),
    recorded_by      VARCHAR(50)  NOT NULL,
    recorded_at      TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_vital_signs_visit ON vital_signs (visit_id, recorded_at DESC);

-- Append-only audit trail of every status change and clinical action on a visit.
CREATE TABLE visit_events (
    id          BIGSERIAL PRIMARY KEY,
    visit_id    BIGINT      NOT NULL REFERENCES visits (id),
    from_status VARCHAR(25),
    to_status   VARCHAR(25) NOT NULL,
    actor       VARCHAR(50) NOT NULL,
    note        TEXT,
    occurred_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_visit_events_visit ON visit_events (visit_id, occurred_at);

-- Daily OPD token numbers (1, 2, 3 ... reset each day), incremented atomically with an upsert.
CREATE TABLE daily_token_counters (
    counter_date DATE    PRIMARY KEY,
    last_value   INTEGER NOT NULL
);
