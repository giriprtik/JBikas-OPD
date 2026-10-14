
📑 System Testing Documentation

Project Module: User Authentication & Authorization Services (JBikas-OPD Backend)
Environment: Dockerized PostgreSQL Sandbox
Testing Framework: JUnit 5, Spring Boot Test, MockMvc

1. Architectural Objectives

The integration test suite was engineered to enforce three strict security boundaries across the application context before any business modules (OPD or Lab) are constructed:
1. Identity Affirmation: Confirm that pre-seeded database accounts can successfully exchange plaintext credentials for structurally signed cryptographical string tokens.
2. Credential Repudiation: Validate that the system actively rejects invalid password inputs, protecting the API from malicious data harvesting.
3. Boundary Integrity: Guarantee that requests lacking valid authorization headers are completely blocked from reading protected endpoints, returning a compliant REST status code.

2. Test Execution Breakdown & Matrix


Test Method Name---Simulated Action---Input Payload / Target Route---Expected HTTP Status---Actual HTTP Status---Result

shouldAuthenticateDefaultDoctorSuccessfully	
Logging in a valid pre-seeded Doctor account.	
POST /api/auth/login { "username": "doctor1", "password": "doctor123" }	
200 OK (with JSON containing Token)	
200 OK	
PASSED ✅

shouldRejectInvalidCredentials	
Logging in an authentic username with an incorrect password.	
POST /api/auth/login { "username": "doctor1", "password": "wrongpassword" }	
401 Unauthorized	
401 Unauthorized	
PASSED ✅

shouldBlockUnauthenticatedRequestsToProtectedModules	
Attempting to access core operational records without a token.	
GET /api/opd/appointments	
401 Unauthorized	
401 Unauthorized	
PASSED ✅


3. Conclusion & Health Verification

The build test run concluded with BUILD SUCCESS (0 Failures, 0 Errors). The data layer successfully handled automatic role assignments (ROLE_DOCTOR, ROLE_NURSE), and the filter correctly routed the HTTP handling behaviors. The security module is stable and safe for production feature integration.