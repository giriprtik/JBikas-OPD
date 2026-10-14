Swagger: http://localhost:8083/swagger-ui.html

📁 lab-service/src/main/java/com/hospital/lab/LabApplication.java
    Purpose: Boots up your third microservice container independently on port 8083 [3.8].

📁 lab-service/src/main/java/com/hospital/lab/config/SecurityConfig.java
	The Entry Gatekeeper: Secures the network boundary for port 8083 [3.8]. It ensures that no external script or client can read or modify laboratory records unless they present a valid, un-tampered cryptographic token issued by the auth-service [3.8].


📁 lab-service/src/main/java/com/hospital/lab/model/LabOrder.java
    The Data Struct: Maps out how a lab record looks inside the isolated jbikas_lab database schema. Instead of using direct database table links (Foreign Keys) to the Patient table, it stores a loose integer tracking reference (patientId) to keep the services completely separate [3.8].

📁 lab-service/src/main/java/com/hospital/lab/repository/LabOrderRepository.java
    The Data Engine: Bridges your Java code with the PostgreSQL storage engine. It automatically handles the heavy lifting of writing SQL statements like SELECT, INSERT, or UPDATE for laboratory records without you having to write raw SQL strings.

📁 lab-service/src/main/java/com/hospital/lab/controller/LabOrderController.java
    The Communication Panel: Exposes public web endpoints (/api/lab/orders/**) to the network [3.8]. It translates incoming JSON payloads from web browsers or other microservices into Java objects and verifies user roles before executing tasks.