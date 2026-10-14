Endpoint: http://localhost:8082
Swagger: http://localhost:8082/swagger-ui.html

📂 Path: opd-service/src/main/java/com/hospital/opd/OpdApplication.java
Purpose: The entry point for this specific microservice. It spins up its own standalone embedded Tomcat server on port 8082.

📂 Path: opd-service/src/main/java/com/hospital/opd/config/JwtUtils.java
Purpose: This utility reads incoming tokens using the newer JJWT 0.12.5 syntax. It validates the signature against the shared secret key string and dynamically extracts the embedded user permissions without touching the external auth-service database [3.8].

📂 Path: opd-service/src/main/java/com/hospital/opd/config/AuthTokenFilter.java
Purpose: Inspects every incoming HTTP request header for Authorization: Bearer <token>. If found, it populates Spring Security's local execution context with the user's role metadata so the API can grant or restrict access instantly.

📂 Path: opd-service/src/main/java/com/hospital/opd/config/SecurityConfig.java
Purpose: Locks down all operational medical endpoints, routing them directly through the AuthTokenFilter. Adding @EnableMethodSecurity allows us to securely restrict specific controllers later—ensuring Nurses can register patients but only Doctors can update treatment notes.



Patient Workflow:
Here is how the patient moves across the three modules:
1. The Entry Check-In (OPD Module): The patient arrives at the Out-Patient Department. A Nurse uses the client interface to log the patient's personal details and primary symptoms into the system. The opd-service saves this information and places the patient into a virtual live waiting queue (QUEUED) [3.8].
2. The Clinical Examination (OPD Module): A Doctor views the active queue list on their screen. They call the patient in, evaluate them, and update their clinical file. If the doctor needs a diagnostic blood panel or scan, they request an automated medical test entry [3.8].
3. The Diagnostic Loop (Lab Module): The system passes a background network request over to the lab-service. A Lab Technician collects the patient's samples, runs the tests, and publishes the official lab results into the isolated lab database. The patient can then return to the doctor with their finalized medical report [3.8].

[ PATIENT ARRIVAL ] 
        │
        ▼
┌────────────────────────────────────────────────────────────────────────┐
│ 1. PATIENT CHECK-IN                                                    │
│    • Patient gives their details to the Nurse at the reception desk.   │
│    • Nurse (Authorized via AUTH-SERVICE) logs the data into the system.│
│                                                                        │
│    ┌───────────────┐           ┌─────────────┐          ┌────────────┐ │
│    │  auth-service │           │ opd-service │          │ PostgreSQL │ │
│    ├───────────────┤           ├─────────────┤          ├────────────┤ │
│    │ Verifies      │ --------> │ Saves name, │ -------> │ Writes to  │ │
│    │ Nurse's Token │           │ age, and    │          │ 'patients' │ │
│    │               │           │ symptoms    │          │ table      │ │
│    └───────────────┘           └─────────────┘          └────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
        │
        ▼
┌────────────────────────────────────────────────────────────────────────┐
│ 2. DOCTOR CONSULTATION                                                 │
│    • Doctor opens the live queue and calls the Patient into the room.  │
│    • Doctor checks the vitals, diagnoses, and orders a Lab Test.       │
│                                                                        │
│                     ┌─────────────┐      REST HTTP      ┌────────────┐ │
│                     │ opd-service │ ──────────────────> │ lab-service│ │
│                     ├─────────────┤                     ├────────────┤ │
│                     │ Changes     │  Triggers background│ Creates a  │ │
│                     │ status to   │  lab request over   │ raw 'lab_  │ │
│                     │ 'REFERRED'  │  the local network  │ order' file│ │
│                     └─────────────┘                     └────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
        │
        ▼
┌────────────────────────────────────────────────────────────────────────┐
│ 3. LABORATORY TESTING                                                  │
│    • Patient goes to the lab room to provide their sample.            │
│    • Technician uploads results, and the Doctor pulls the file.        │
│                                                                        │
│    ┌──────────────────┐           ┌────────────┐        ┌────────────┐ │
│    │   lab-service    │           │ PostgreSQL │        │[ DISCHARGE]│ │
│    ├──────────────────┤           ├────────────┤        ├────────────┤ │
│    │ Updates order to │ --------> │ Writes data│ -----> │ Patient    │ │
│    │ 'COMPLETED' with │           │ to 'test_  │        │ receives   │ │
│    │ test records     │           │ reports'   │        │ prescription││
│    └──────────────────┘           └────────────┘        └────────────┘ │
└────────────────────────────────────────────────────────────────────────┘

