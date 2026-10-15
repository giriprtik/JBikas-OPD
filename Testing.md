# JBikas End-to-End Test Plan

Walks one patient through the whole hospital flow: registration → triage → consultation → lab → report → completion.
Use the UI at http://localhost:5173, or Swagger on each service (`/swagger-ui.html`) with the Bearer token from login.

## 0. Start clean

```bash
docker compose down -v          # first run after the upgrade: old tables are incompatible
docker compose up --build
```

Check that every service started and Flyway ran:

```bash
docker compose logs opd-service lab-service | grep -i "Successfully applied"
```

Seeded accounts: `nurse1/nurse123`, `doctor1/doctor123`, `labtech1/lab123`, `admin1/admin123`.

## 1. Auth (8081)

| Step | Request | Expect |
|---|---|---|
| Login | `POST /api/auth/login {"username":"doctor1","password":"doctor123"}` | 200, body has `token`, `fullName`, `roles` |
| Bad password | same with a wrong password | **401** "Incorrect username or password." |
| Self sign-up blocked | `POST /api/auth/register` without a token | 401 |
| Admin creates user | as `admin1`: `POST /api/auth/register {"username":"sita_lab","password":"securepass1","fullName":"Sita Thapa","role":"LAB_TECH"}` | 201 |
| Duplicate | same again | 409 "Username 'sita_lab' is already taken." |

## 2. Nurse (UI: /nurse)

1. **Register visit → Find patient**: search "Madan". Nothing found → **New patient**:
   name *Madan Krishna Shrestha*, age 62, Male, phone 9841987654, allergies *Penicillin*.
   Expect MRN `JB-000001`.
2. Register the same name and phone again → **409** pointing to `JB-000001`.
3. **Open visit**: complaint "Polyuria and fatigue for 2 months", priority Normal → token **1**.
   Opening a second visit for the same patient → **409** (one open visit per patient).
4. **Triage**: select token 1, enter Temp 37.0, Pulse 88, BP 150/95, SpO₂ 97 → BP flagged **H** in red.
   Save → status *Waiting for doctor*.
5. Enter SpO₂ **86** on another patient → shows **LL** and the "Critical value" warning; after saving,
   a Normal priority is raised to **Urgent** automatically.

## 3. Doctor (UI: /doctor)

1. The queue shows Emergency first, then Urgent, then Normal by arrival. Open Madan → **Start consultation**.
   The allergy banner (Penicillin) is visible.
2. Write notes, leave Diagnosis empty → **Complete visit** is disabled.
3. **Order tests**: FBS, RFT, HbA1c, priority Urgent, notes "?Type 2 DM" → order `LAB-yyMMdd-00001`,
   status *Awaiting lab*. The lab card shows "Results are shown here once the laboratory releases the report."
4. API check: `POST /api/opd/visits/{id}/lab-orders` as a **nurse** → 403.

## 4. Lab technician (UI: /lab)

1. **To collect** shows the order (Urgent badge) → **Sample collected**.
2. **In progress** → enter:

   | Parameter | Value | Expected flag |
   |---|---|---|
   | Glucose, fasting | 212 | **H** (70–100) |
   | Urea | 32 | normal |
   | Creatinine | 1.05 | normal |
   | Sodium | 138 | normal |
   | Potassium | **6.9** | **HH** critical (2.5 / 6.5), red row |
   | HbA1c | 8.1 | **H** |

   Type `13o` in a numeric field → "Not a number", Save is disabled.
3. **Save draft** → worklist shows 6/6 entered, 1 critical.
4. **Release report** → the dialog lists the critical potassium and requires "Critical values phoned to".
   Release without it via API → **409**. Enter "Dr. Shrestha, 14:20" → Released.
5. **PDF report** → glucose/HbA1c bold red with H, potassium red row with HH, the critical statement,
   and "Released by Lab Tech Gurung".
6. **Attachments**: upload a PDF → listed. Upload a `.txt` renamed to `.pdf` → rejected (content check).
7. **Amend**: on the released Potassium enter 5.0 with reason "Haemolysed sample, repeated" → row no
   longer critical, marked `*`; the PDF lists the amendment.
8. Preview the report of an unreleased order → **PRELIMINARY** watermark. As doctor via API → 409.

## 5. Back to the doctor

1. The queue shows Madan as **Lab results ready**, raised to **Urgent** because a critical value was released.
2. Open → **Resume consultation** → the lab card shows flagged results and the PDF button.
3. Diagnosis "Type 2 diabetes mellitus; hyperkalaemia, resolved on repeat" → **Complete visit**.
4. The visit timeline lists every step with actor and time.

## 6. Regression checks

- Sign out and use the browser back button → returns to login.
- Leave a tab past token expiry (`JWT_EXPIRATION`) → the next request signs out automatically.
- Stop lab-service, order a test → 502 "The laboratory service is not reachable…"; the visit stays *In consultation*.
- Refresh on `/doctor` inside the Docker frontend → page loads (nginx SPA fallback).
- Run the unit tests: `mvn test` in `opd-service` and `lab-service`. They cover flags, ranges, vitals and the workflow.
