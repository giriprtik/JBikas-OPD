• Backend REST API: Accessible at http://localhost:8080
• React UI Application: Accessible at http://localhost:5173
    Swagger: http://localhost:8080/swagger-ui.html

backend/
├── pom.xml
└── src/
    └── main/
        ├── java/com/hospital/opd/
        │   ├── OpdApplication.java      # Application Bootstrapper
        │   │
        │   ├── common/                  # Cross-cutting concerns
        │   │   ├── config/              # SecurityConfig, CorsConfig
        │   │   └── exceptions/          # GlobalExceptionHandler
        │   │
        │   ├── auth/                    # Authorization Module (JWT)
        │   │   ├── controller/          # AuthController (Login/Register endpoints)
        │   │   ├── model/               # User, Role entities
        │   │   ├── repository/          # UserRepository
        │   │   ├── service/             # UserService, CustomUserDetailsService
        │   │   └── jwt/                 # JwtFilter, JwtTokenProvider, JwtEntryPoint
        │   │
        │   ├── opd/                     # Out-Patient Department Module
        │   │   ├── controller/          # OpdController, PatientController
        │   │   ├── model/               # Patient, Appointment, Consultation entities
        │   │   ├── repository/          # PatientRepository, AppointmentRepository
        │   │   └── service/             # OpdService (Handles workflow/doctor routing)
        │   │
        │   └── lab/                     # Laboratory Module
        │       ├── controller/          # LabController
        │       ├── model/               # LabTest, TestReport, Sample entities
        │       ├── repository/          # LabRepository
        │       └── service/             # LabService (Interfaces with OPD requests)
        │
        └── resources/
            ├── application.yml          # PostgreSQL Database and JWT secret configs
            └── db/migration/            # (Optional) SQL schema scripts for PostgreSQL


frontend/
├── package.json
├── vite.config.js
├── public/
└── src/
    ├── main.jsx                 # Entry point
    ├── App.jsx                  # Main routing infrastructure
    ├── index.css                # Tailwind or general style setup
    │
    ├── api/                     # Axios setup with interceptors to auto-attach the JWT token
    │   └── axiosClient.js
    │
    ├── components/              # Globally shared layout structures
    │   ├── Navbar.jsx
    │   ├── Sidebar.jsx
    │   └── ProtectedRoute.jsx   # Blocks unauthenticated access via JWT check
    │
    ├── context/
    │   └── AuthContext.jsx      # Provides global user state (Token, Roles) to the app
    │
    └── modules/                 # Modular layout matching the backend
        ├── auth/
        │   ├── pages/Login.jsx
        │   └── components/LoginForm.jsx
        ├── opd/
        │   ├── pages/OpdDashboard.jsx
        │   ├── pages/PatientRegistration.jsx
        │   └── components/QueueTable.jsx
        └── lab/
            ├── pages/LabDashboard.jsx
            ├── pages/ReportGeneration.jsx
            └── components/TestOrderList.jsx
