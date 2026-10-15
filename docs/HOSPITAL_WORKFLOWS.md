# JBikas OPD & Lab — Hospital Workflows (Developer Guide)

This document describes how the system works after the hospital-workflow upgrade: roles, business rules,
service responsibilities, data model, and what is still needed before go-live. Read it before changing
any workflow code.

## 1. Services at a glance

| Service | Port | Owns | Database |
|---|---|---|---|
| auth-service | 8081 | Staff accounts, roles, JWT issuing | `jbikas_auth` (Hibernate `ddl-auto: update`) |
| opd-service | 8082 | Patients (MRN), visits, triage vitals, consultation, visit audit trail | `jbikas_opd` (Flyway) |
| lab-service | 8083 | Test catalog + reference ranges, lab orders, results + flags, amendments, attachments, PDF reports | `jbikas_lab` (Flyway) |
| frontend-ui | 5173 | React UI for nurse, doctor and lab technician | — |

All services verify the same HMAC-signed JWT. Claims: `sub` (username), `roles` (comma-separated, e.g.
`ROLE_DOCTOR`), `name` (display name), `exp`.

### Cross-service calls

```
Doctor UI ──POST /api/opd/visits/{id}/lab-orders──▶ opd-service ──POST /api/lab/orders──▶ lab-service
                                                     (doctor's token forwarded)
Lab UI ──POST /api/lab/orders/{id}/release──▶ lab-service ──POST /api/opd/visits/{visitId}/lab-results-ready──▶ opd-service
                                              (after commit, lab tech's token forwarded, best effort)
```

- **OPD → Lab** is synchronous. If lab-service is down, the doctor gets a 502 with a clear message and the
  visit status does not change.
- **Lab → OPD** runs after the release transaction commits and never fails the release. If OPD is
  unreachable the visit stays `AWAITING_LAB`, but the doctor still sees the released results in the visit's lab panel.

## 2. Roles

| Role | Screen | Can do |
|---|---|---|
| `ROLE_NURSE` | `/nurse` | Search/register patients, open visits (tokens), record vitals and triage priority, cancel visits not yet seen |
| `ROLE_DOCTOR` | `/doctor` | Work the queue, start/resume consultations, notes and diagnosis, order lab tests, view released results and PDFs, complete visits |
| `ROLE_LAB_TECH` | `/lab` | Worklist, collect samples, enter results, upload files, release reports, amend released results, cancel orders |
| `ROLE_ADMIN` | (API only) | `POST /api/auth/register` to create staff accounts, `GET /api/auth/users` |

Self sign-up is removed: `/api/auth/register` requires an admin token.

Seeded development accounts are `admin1/admin123`, `doctor1/doctor123`, `nurse1/nurse123` and `labtech1/lab123`.
Disable them with `APP_SEED_DEFAULT_USERS=false`.

## 3. OPD workflow

### Patient registry
- Each patient gets a **medical record number** `JB-000001` from the Postgres sequence `patient_mrn_seq`.
- Date of birth is stored, not age. If only an age is known, DOB is estimated as 1 January of the birth
  year and `dob_estimated = true`. Age is then always computed for "today", so it never goes stale.
- **Duplicate guard.** Registering the same name and phone again is rejected with the existing MRN.
- **Search.** `JB-…` matches the MRN exactly; digits match the phone partially; anything else matches the name (case-insensitive). At most 20 results.

### Visit lifecycle (`VisitStatus`)

```
REGISTERED ──vitals──▶ TRIAGED ──doctor──▶ IN_CONSULTATION ──complete──▶ COMPLETED
    │  └──(EMERGENCY only)──────────────────────▲     │   ▲
    │                                          order  │   │ resume
    ▼                                           lab   ▼   │
 CANCELLED ◀── (REGISTERED / TRIAGED)        AWAITING_LAB ──released──▶ LAB_RESULTS_READY
```

Rules (enforced in `VisitStatus.canMoveTo` and `VisitService`):
- **One open visit per patient.** Enforced in the service, and also by a partial unique index in the database (`uq_visits_one_active_per_patient`).
- **Tokens.** Token numbers restart at 1 each day in hospital time (`APP_TIMEZONE`). They come from an atomic upsert on `daily_token_counters`.
- **First vitals.** The first set of vitals moves a visit `REGISTERED → TRIAGED`. Vitals can be recorded again at any time while the visit is open.
- **Critical vitals.** Any critical vital raises a `NORMAL` priority to `URGENT` automatically.
- **Emergencies before triage.** A doctor can only start a consultation on a `REGISTERED` (un-triaged) visit when its priority is `EMERGENCY`.
- **Doctor ownership.** Only the assigned doctor can write notes, order labs or complete the visit. Another doctor can take over a visit that is back in the queue (`AWAITING_LAB` / `LAB_RESULTS_READY`); the takeover is logged.
- **Completing.** A visit can only be completed with a diagnosis.
- **Cancelling.** Only visits the doctor has not seen yet (`REGISTERED` / `TRIAGED`) can be cancelled, and a reason is required.
- **Critical lab results.** When released, they raise a `NORMAL` visit to `URGENT`, so the patient moves up the queue.
- **Audit trail.** Every change writes an append-only `visit_events` row: who, when, from/to status, and a note.

**Doctor queue order:** EMERGENCY, then URGENT, then NORMAL; within each, earliest arrival first.

### Vitals flagging (`VitalsAssessor`)
These are adult limits; crossing the critical limit sets `CRITICAL_*`.

| Vital | Normal | Critical |
|---|---|---|
| Temperature | 36.1–37.5 °C | <35.0 or >40.0 |
| Pulse | 60–100 bpm | <40 or >140 |
| Respiratory rate | 12–20 /min | <8 or >30 |
| Systolic BP | 90–139 mmHg | <80 or >180 |
| Diastolic BP | 60–89 mmHg | <40 or >120 |
| SpO₂ | ≥95 % | <90 |

For patients under 12, only the critical limits are applied. Paediatric normal ranges are a to-do (§7).

## 4. Lab workflow

### Catalog and reference ranges
- `test_definitions` holds orderable tests (CBC, FBS, RBS, HbA1c, RFT, LFT, Lipid, TFT, Urine RE, Chest X-ray).
  `test_parameters` holds what is measured, and `reference_ranges` holds the limits by sex and age band.
- The seed is generated by `tools/gen_lab_catalog.py` → `V2__seed_test_catalog.sql`.
  **The pathologist must review every range** before go-live, because ranges depend on the hospital's analysers and reagents.
- **Parameter types:**
  - `NUMERIC`: flagged against the low/high and critical limits.
  - `TEXT`: flagged against a list of normal values (e.g. Protein `Nil, Negative`; anything else is `ABNORMAL`).
  - `NARRATIVE`: free text (X-ray findings), never flagged.

**Range selection** (`ReferenceRangeResolver`):
1. Only ranges whose age band contains the patient's age are considered. Bands are `[min, max)`.
2. A range for the patient's own sex wins over an `ANY` range.
3. Among equals, the narrowest age band wins.
4. For sex `OTHER`, the union of the male and female ranges is used, so that a value normal for either sex is not flagged.

The resolved limits are **copied into each `lab_results` row when the order is placed**. Later catalog edits never change how an existing report is interpreted.

### Flags (`ResultFlagEvaluator`, the single source of truth)
`CRITICAL_LOW` < critical low ≤ `LOW` < low ≤ `NORMAL` ≤ high < `HIGH` ≤ critical high < `CRITICAL_HIGH`

Limits are inclusive: a value equal to a limit counts as normal. `ABNORMAL` is used for text results;
`NONE` means no interpretation (no range, or narrative). The UI previews the same rules while the technician
types (`previewResultFlag` in `domain/clinical.js`), but the stored flag always comes from the server.

### Order lifecycle (`LabOrderStatus`)

```
ORDERED ──collect──▶ SAMPLE_COLLECTED ──first save──▶ IN_PROGRESS ──release──▶ RELEASED ──(amend)──▶ RELEASED*
   └──────────────────────┴───────────────────────────────┴──▶ CANCELLED (reason required)
```

- Order numbers look like `LAB-261014-00042`: the day in hospital time plus a global sequence. Use the number as the sample label.
- **Release** requires a value for every NUMERIC and TEXT parameter, and at least one value per test.
- **Critical values** block release until the technician records who was phoned (`criticalNotifiedTo`). That name is printed on the report.
- **Who sees values:** doctors only see result values once an order is `RELEASED`; lab technicians always see them.
- **Amendments** apply to released results only, require a reason, and are stored in `result_amendments` with the old and new value and flag. The report marks amended rows with `*` and lists every change.
- **Attachments** (PDF/PNG/JPEG, 10 MB by default):
  - The type is detected from the file's first bytes, not from its name or the browser's Content-Type.
  - Files are stored under a random UUID on the `lab-attachments` volume, never under the uploaded file name.
  - They can be removed until the report is released.

### PDF report (`ReportService`, OpenPDF)
- **Header and patient block:** hospital name/address (`LAB_REPORT_HOSPITAL_*`), patient identity, order number, referring doctor, and the ordered/collected/reported times.
- **Results tables:** one per test with value, flag, unit and reference range.
  - Abnormal values are printed **bold red** with `H` / `L` / `A`.
  - Critical values are bold red with `HH` / `LL` on a red row.
- **Footnotes:** the critical-communication statement, amendments, a legend, and who released the report.
- **Drafts:** a lab tech can preview an unreleased report, which carries a **PRELIMINARY** watermark. Doctors can only open released reports.

## 5. Database changes

### What changed
- **auth (`jbikas_auth`).** No schema change; the `user_roles.role` column now also holds `ROLE_LAB_TECH` and `ROLE_ADMIN`.
- **opd (`jbikas_opd`).** The single `patients` table is replaced by Flyway migration `V1__opd_initial_schema.sql`:
  - tables: `patients`, `visits`, `vital_signs`, `visit_events`, `daily_token_counters`
  - sequence: `patient_mrn_seq`
  - check constraints and indexes, plus the partial unique index for one open visit per patient
- **lab (`jbikas_lab`).** The single `lab_orders` table is replaced by Flyway migrations `V1` (schema) and `V2` (catalog seed):
  - tables: `test_definitions`, `test_parameters`, `reference_ranges`, `lab_orders`, `lab_order_items`, `lab_results`, `result_amendments`, `lab_attachments`
  - sequence: `lab_order_number_seq`
- **ddl-auto.** opd and lab now run with `ddl-auto: none`; Flyway is the only thing that changes their schema.
- **Time.** All timestamps are `TIMESTAMPTZ` (UTC instants). "Today" is decided in `APP_TIMEZONE` (default `Asia/Kathmandu`).

### Applying it (existing development database)
The old tables were created by Hibernate. Flyway will not run on a non-empty schema without a history table,
and the old data does not fit the new model. For development data:

```bash
docker compose down -v      # drops pgdata (and lab-attachments) volumes
docker compose up --build
```

`init-scripts/init.sql` recreates the three databases, then each service runs its migrations on start.
If the old data must be kept, export it first and write a one-off import into the new tables. Do not
point Flyway at the old schema.

### Rules for future changes
- Never edit an applied migration. Add `V3__description.sql` and so on.
- Keep entity mappings and migrations in step. Once the schema is stable, consider `ddl-auto: validate` in a test profile to catch drift.
- Back up the `lab-attachments` volume together with `jbikas_lab`; one is useless without the other.

## 6. API summary

OPD (`/api/opd`):
`POST /patients` · `PUT /patients/{id}` · `GET /patients/search?q=` · `GET /patients/{id}` · `GET /patients/{id}/visits` ·
`GET /visits?date=&status=` · `GET /visits/doctor-queue` · `GET /visits/{id}` · `POST /visits` · `POST /visits/{id}/vitals` ·
`POST /visits/{id}/cancel` · `POST /visits/{id}/start-consultation` · `PUT /visits/{id}/consultation` ·
`POST /visits/{id}/lab-orders` · `POST /visits/{id}/complete` · `POST /visits/{id}/lab-results-ready` (lab-service only)

Lab (`/api/lab`):
`GET /catalog` · `POST /orders` (via OPD) · `GET /orders?status=` · `GET /orders/{id}` · `GET /orders/by-visit/{visitId}` ·
`GET /orders/by-patient/{patientId}` · `POST /orders/{id}/collect-sample` · `PUT /orders/{id}/results` ·
`POST /orders/{id}/release` · `POST /orders/{id}/results/{resultId}/amend` · `POST /orders/{id}/cancel` ·
`GET /orders/{id}/report` (PDF) · `POST /orders/{id}/attachments` (multipart) · `GET|DELETE /orders/{id}/attachments/{attachmentId}`

Errors are uniform: `{ status, error, message, fieldErrors, timestamp }`. `message` is written for end users;
the UI shows it as-is for 4xx responses. The status codes are:

| Code | Meaning |
|---|---|
| 400 | Validation error |
| 401 | Missing or expired token |
| 403 | Wrong role |
| 404 | Record not found |
| 409 | Business rule or concurrent edit (optimistic locking on `visits` and `lab_orders`) |
| 413 | File too large |
| 502 | Another service is down |

## 7. Not done yet — needed before real patients

**Clinical safety**
1. **Pathologist sign-off.** The pathologist must approve the reference ranges and critical limits (`V2__seed_test_catalog.sql`). Apply changes as a new migration.
2. **Physician sign-off on vitals.** The medical team must approve the vitals thresholds; paediatric vitals ranges by age band also still need to be added.
3. **Two-person verification.** Optionally, require a second technician or pathologist to verify results before release; currently one technician enters and releases.
4. **Delta checks.** Flag big changes from the patient's previous result. The data is already available via `GET /orders/by-patient/{id}`.

**Security and compliance**
5. **Secrets.** Move the JWT secret and DB passwords out of `docker-compose.yml` into an `.env` file or secret store, and rotate them. The current secret is committed to git.
6. **HTTPS.** Put HTTPS in front of every service, for example with a reverse proxy. Restrict CORS through `APP_CORS_ALLOWED_ORIGINS`.
7. **Admin UI.** Add a screen for user management: create users, deactivate them and reset passwords. There is also no "deactivate user" flag yet.
8. **Read audit.** Writes are audited (`visit_events`, `result_amendments`); reads are not. Many regulations expect access logs for patient records.
9. **Token handling.** The token is kept in `localStorage`. Consider shorter tokens with refresh, or an httpOnly-cookie session, on shared ward PCs.

**Reliability and operations**
10. **Outbox for cross-service calls.** If the OPD database commit fails after the lab order was created, the lab order is orphaned; a lab technician can cancel it. An outbox table or a message broker (RabbitMQ/Kafka) would make both directions reliable and retryable.
11. **Shared security module.** `JwtUtils`, `AuthTokenFilter` and `StaffPrincipal` are duplicated in opd and lab. Extract them into a shared library.
12. **End-of-day job.** Close visits left open overnight; until then they block that patient from opening a new visit.
13. **Backups and monitoring.** Set up Postgres and attachment-volume backups, health endpoints (Spring Actuator), and centralised logs.
14. **Integration tests.** Add Testcontainers tests for the controllers and migrations. The unit tests cover the flag, range, vitals and workflow rules.

**Features hospitals usually expect next**
15. Billing and payment before sample collection, prescriptions/pharmacy, appointments, departments with multiple OPD rooms, printable sample labels and barcodes, analyser (LIS/HL7) interfacing, SMS for ready reports, and a Devanagari font in the PDF (`ReportService.pdf()` currently maps a few symbols to the built-in Latin font).
