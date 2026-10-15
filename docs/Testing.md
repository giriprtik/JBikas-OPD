JBikas API Verification & Test Plan

This document maps out how to test our new microservices architecture across the Auth, OPD, and Lab modules using their independent Swagger UI pages.

Service Ports Overview

Make sure your Docker containers are running. We are using isolated databases for each service, bound to these local ports:
• Auth Service: http://localhost:8081/swagger-ui.html
• OPD Service: http://localhost:8082/swagger-ui.html
• Lab Service: http://localhost:8083/swagger-ui.html

Step-by-Step Test Execution Flow


Phase 1: Authentication & Token Retrieval (Port 8081)

We will fetch the Bearer tokens for our two pre-seeded accounts. These tokens will be used to pass through the security filters on the other modules.

1. Log in as a Doctor

• Endpoint: POST /api/auth/login
• Payload:json
{
  "username": "doctor1",
  "password": "doctor123"
}
• Expected Result: 200 OK. Copy the value inside the "token" field from the JSON response. Save this text block as your Doctor Token.

2. Log in as a Nurse

• Endpoint: POST /api/auth/login
• Payload:json
{
  "username": "nurse1",
  "password": "nurse123"
}
• Expected Result: 200 OK. Copy the value inside the "token" field. Save this text block as your Nurse Token.

3. Test Registering a New Account

• Endpoint: POST /api/auth/register
• Payload:json
{
  "username": "sita_nurse",
  "password": "securepassword987",
  "fullName": "Sita Thapa",
  "role": "NURSE"
}
• Expected Result: 200 OK with text "User registered successfully!".
• Note: If you run this exact payload a second time, it should fail with a 400 Bad Request ("Error: Username is already taken!").

Phase 2: Patient Registration & Queue (Port 8082)

Now we switch to the OPD service. All endpoints here are locked down by role restrictions.

1. Setup Authorization

Go to http://localhost:8082/swagger-ui.html. Click the green Authorize padlock button at the top, paste the Nurse Token, click Authorize, and close the dialog box.

2. Register a Patient (Nurse Only)

• Endpoint: POST /api/opd/patients/register
• Payload:json
{
  "name": "Madan Krishna Shrestha",
  "age": 62,
  "gender": "Male",
  "contactNumber": "9841987654",
  "chiefComplaint": "Persistent dry cough and high fever for 3 days."
}
Use code with caution.
• Expected Result: 200 OK. Take note of the returning database "id" integer (this will usually be 1). The patient's status will default to "QUEUED".

3. Verify Role Enforcement Block

Click the Authorize lock button at the top again, hit Logout, and paste the Doctor Token instead. Go back down to POST /api/opd/patients/register and try to submit a new patient.
• Expected Result: 403 Forbidden. The backend security filter should actively block doctors from administrative check-ins.

Phase 3: Cross-Service Lab Referral (Port 8082 ➔ Port 8083)

We will now test the internal HTTP network bridge between the services. Keep your Doctor Token authorized in the port 8082 Swagger page.

1. Order a Lab Test

• Endpoint: PUT /api/opd/patients/{id}/order-lab
• Parameters:
	• id: 1 (or the patient ID generated in Phase 2)
	• testType: Chest X-Ray and CBC
• Expected Result: 200 OK. The screen will display: "Lab order triggered successfully via microservice bridge!".
• Database Check: If you call GET /api/opd/patients/all, you will see this patient's status has automatically changed from "QUEUED" to "REFERRED_TO_LAB".

Phase 4: Lab Order Processing & Results (Port 8083)

Finally, we verify that the order successfully traveled across the network and landed in the laboratory module's isolated database.

1. Setup Lab Authorization

Go to http://localhost:8083/swagger-ui.html. Click the green Authorize padlock, paste your Doctor Token, and close.

2. Fetch Pending Tests

• Endpoint: GET /api/lab/orders/pending
• Expected Result: 200 OK. The response will return a JSON array containing the record sent from OPD:json
[
  {
    "id": 1,
    "patientId": 1,
    "patientName": "Madan Krishna Shrestha",
    "testType": "Chest X-Ray and CBC",
    "testResult": null,
    "status": "PENDING"
  }
]

• Capture the value inside this object's "id" field (this is your Lab Order ID, usually 1).

3. Upload Finalized Lab Results

• Endpoint: PUT /api/lab/orders/{id}/complete
• Parameters:
	• id: 1 (or your Lab Order ID)
	• Request body (Paste as raw text string):text
"Chest X-Ray shows mild lung congestion. CBC indicates elevated White Blood Cell count (12,500/mcL)."
Use code with caution.
• Expected Result: 200 OK. The item status updates to "COMPLETED", wrapping up the full distributed cycle.
