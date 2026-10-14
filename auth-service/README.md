Swagger: http://localhost:8081/swagger-ui.html
Database: http://localhost:8085

📂 Path: auth-service/src/main/java/com/hospital/auth/AuthApplication.java
Purpose: Serves as the bootstrapping anchor for this microservice. It initializes the embedded Tomcat web server on port 8081 [3.8] and scans all local sub-packages for Spring components.

📂 Path: auth-service/src/main/java/com/hospital/auth/model/User.java
Purpose: Maps the data layer structure inside the isolated jbikas_auth PostgreSQL schema [3.8]. By using an @ElementCollection for roles, a user can scale to hold multiple authorities if needed.

📂 Path: auth-service/src/main/java/com/hospital/auth/repository/UserRepository.java
• Purpose: Inherits CRUD abstraction mechanisms from Spring Data JPA. It enables checking for duplicate registration items and loading user data dynamically from the database during authentication execution checks.

📂 Path: auth-service/src/main/java/com/hospital/auth/jwt/JwtUtils.java
Purpose: Handles cryptographic operations for signing tokens. Crucially, it embeds user roles inside the token claims structure. This step allows other downstream microservices to read user permissions directly out of the string packet without querying the database.

📂 Path: auth-service/src/main/java/com/hospital/auth/config/SecurityConfig.java
Purpose: Configures stateless request handling for the authentication endpoints. It also registers a standard BCryptPasswordEncoder to safely hash user passwords before storing them.

📂 Path: auth-service/src/main/java/com/hospital/auth/service/CustomUserDetailsService.java
Purpose: Bridges Spring Security's authentication manager with your custom PostgreSQL database records. It retrieves user credentials and transforms them into standard UserDetails objects for password verification.

📂 Path: auth-service/src/main/java/com/hospital/auth/controller/AuthController.java
Purpose: Exposes public endpoints (/api/auth/login and /api/auth/register) [3.8]. It validates payloads, safely hashes plain text passwords, manages the login flow, and returns the generated JWT string response back to the client app.

📁 auth-service/src/main/java/com/hospital/auth/config/DataInitializer.java
Purpose: Runs once automatically when the auth-service container starts. It checks the PostgreSQL jbikas_auth schema and registers the default doctor1 and nurse1 credentials securely using BCrypt if the tables are empty.

📁 auth-service/src/main/resources/application.yml
Purpose: Sets the service port explicitly to 8081, targets the isolated jbikas_auth relational database instance schema [3.8], and binds the shared cryptographic secret parameters.

📁 init-scripts/init.sql
Purpose: Injects a small initialization shell script directly into the PostgreSQL container. This script automatically runs whenever the database container spawns, checking for and initializing your three microservice schemas (jbikas_auth, jbikas_opd, and jbikas_lab) seamlessly.


DATABASE:
               ┌─── [ jbikas-postgres Container ] ───┐
               │                                      │
  ┌────────────┼───────────┐   ┌──────────┼───────────┐   ┌────────────┼───────────┐
  │ jbikas_auth Schema     │   │ jbikas_opd Schema    │   │ jbikas_lab Schema      │
  ├────────────────────────┤   ├──────────────────────┤   ├────────────────────────┤
  │ • users                │   │ • patients           │   │ • lab_orders           │
  │ • user_roles           │   │ • appointments       │   │ • test_results         │
  └────────────┬───────────┘   └──────────┬───────────┘   └────────────┼───────────┘
               │                          │                            │
      (auth-service Only)         (opd-service Only)           (lab-service Only)


WORKFLOW:
# 🏥 Nurse Authentication & OPD Queue Workflow

---

### 🔑 Phase 1: Authentication & Token Issuance

> **1. Submit Credentials**
> 📥 `Nurse UI Screen` ── **POST** `/login` (JSON Body) ──> `Auth-Service`
 
> **2. Database Match**
> 🔍 `Auth-Service` ── Queries & verifies hashed password ──> `PostgreSQL`

> **3. Sign Token**
> 📝 `Auth-Service` generates a stateful **JWT Token** containing `ROLE_NURSE`.

> **4. Token Return**
> 📤 `Nurse UI Screen` <── **Returns Token (String)** ── `Auth-Service`

---

### 📋 Phase 2: Secure Resource Access (OPD Queue)

> **5. Request Patient Data**
> 📥 `Nurse UI Screen` ── **GET** `/api/opd/queue` *(Bearer Token Header)* ──> `OPD-Service`

> **6. Local Verification**
> 🔐 `OPD-Service` cryptographically checks the signature via a **Shared Secret Key**.

> **7. Schema Query**
> 🔍 `OPD-Service` ── Fetches active records ──> `PostgreSQL`

> **8. View Delivery**
> 📤 `Nurse UI Screen` <── **Returns Patient List** ── `OPD-Service`

---

