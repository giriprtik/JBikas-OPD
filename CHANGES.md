# JBikas hospital workflows: change manifest

Copy both folders over the repository root (paths are preserved), delete the files listed below, then reset the dev database:

```bash
cp -r new-files/. updated-files/. /path/to/JBikas-OPD/
cd /path/to/JBikas-OPD
xargs git rm < DELETE-THESE.txt
docker compose down -v && docker compose up --build
```

Or apply the git patch instead: `git am jbikas-hospital-workflows.patch` (on top of the UI-upgrade commit).

## Delete (7)
These are replaced by new files (for example, `config/JwtUtils.java` moved to `security/JwtUtils.java`).

- `frontend-ui/src/api/patientApi.js`
- `frontend-ui/src/modules/opd/patientFormat.js`
- `frontend-ui/src/modules/opd/usePatientList.js`
- `lab-service/src/main/java/com/hospital/lab/config/AuthTokenFilter.java`
- `lab-service/src/main/java/com/hospital/lab/config/JwtUtils.java`
- `opd-service/src/main/java/com/hospital/opd/config/AuthTokenFilter.java`
- `opd-service/src/main/java/com/hospital/opd/config/JwtUtils.java`

## New files (134)


**auth-service**

- `auth-service/src/main/java/com/hospital/auth/dto/LoginRequest.java`
- `auth-service/src/main/java/com/hospital/auth/dto/LoginResponse.java`
- `auth-service/src/main/java/com/hospital/auth/dto/RegisterRequest.java`
- `auth-service/src/main/java/com/hospital/auth/dto/UserResponse.java`
- `auth-service/src/main/java/com/hospital/auth/exception/ApiError.java`
- `auth-service/src/main/java/com/hospital/auth/exception/BusinessRuleException.java`
- `auth-service/src/main/java/com/hospital/auth/exception/GlobalExceptionHandler.java`
- `auth-service/src/main/java/com/hospital/auth/jwt/AuthTokenFilter.java`

**docs**

- `docs/HOSPITAL_WORKFLOWS.md`

**frontend-ui**

- `frontend-ui/.dockerignore`
- `frontend-ui/.env.example`
- `frontend-ui/nginx.conf`
- `frontend-ui/src/api/errors.js`
- `frontend-ui/src/api/labApi.js`
- `frontend-ui/src/api/opdApi.js`
- `frontend-ui/src/components/clinical/index.jsx`
- `frontend-ui/src/domain/clinical.js`
- `frontend-ui/src/hooks/useAction.js`
- `frontend-ui/src/hooks/useAsync.js`
- `frontend-ui/src/hooks/useInterval.js`
- `frontend-ui/src/hooks/useRevealOnNarrow.js`
- `frontend-ui/src/modules/lab/AttachmentsPanel.jsx`
- `frontend-ui/src/modules/lab/LabDashboard.jsx`
- `frontend-ui/src/modules/lab/OrderWorkspace.jsx`
- `frontend-ui/src/modules/lab/ResultEntryForm.jsx`
- `frontend-ui/src/modules/opd/doctor/ConsultationWorkspace.jsx`
- `frontend-ui/src/modules/opd/doctor/LabOrderCard.jsx`
- `frontend-ui/src/modules/opd/doctor/LabOrderForm.jsx`
- `frontend-ui/src/modules/opd/nurse/PatientForm.jsx`
- `frontend-ui/src/modules/opd/nurse/RegisterVisit.jsx`
- `frontend-ui/src/modules/opd/nurse/TodayVisits.jsx`
- `frontend-ui/src/modules/opd/nurse/TriagePanel.jsx`

**lab-service**

- `lab-service/src/main/java/com/hospital/lab/client/OpdClient.java`
- `lab-service/src/main/java/com/hospital/lab/config/AppConfig.java`
- `lab-service/src/main/java/com/hospital/lab/controller/CatalogController.java`
- `lab-service/src/main/java/com/hospital/lab/dto/AmendRequest.java`
- `lab-service/src/main/java/com/hospital/lab/dto/AttachmentResponse.java`
- `lab-service/src/main/java/com/hospital/lab/dto/CancelRequest.java`
- `lab-service/src/main/java/com/hospital/lab/dto/CatalogParameterResponse.java`
- `lab-service/src/main/java/com/hospital/lab/dto/CatalogTestResponse.java`
- `lab-service/src/main/java/com/hospital/lab/dto/CreateLabOrderRequest.java`
- `lab-service/src/main/java/com/hospital/lab/dto/LabItemResponse.java`
- `lab-service/src/main/java/com/hospital/lab/dto/LabOrderResponse.java`
- `lab-service/src/main/java/com/hospital/lab/dto/LabOrderSummary.java`
- `lab-service/src/main/java/com/hospital/lab/dto/LabResultResponse.java`
- `lab-service/src/main/java/com/hospital/lab/dto/ReleaseRequest.java`
- `lab-service/src/main/java/com/hospital/lab/dto/ResultEntryRequest.java`
- `lab-service/src/main/java/com/hospital/lab/exception/ApiError.java`
- `lab-service/src/main/java/com/hospital/lab/exception/BusinessRuleException.java`
- `lab-service/src/main/java/com/hospital/lab/exception/DownstreamServiceException.java`
- `lab-service/src/main/java/com/hospital/lab/exception/GlobalExceptionHandler.java`
- `lab-service/src/main/java/com/hospital/lab/exception/NotFoundException.java`
- `lab-service/src/main/java/com/hospital/lab/model/LabAttachment.java`
- `lab-service/src/main/java/com/hospital/lab/model/LabOrderItem.java`
- `lab-service/src/main/java/com/hospital/lab/model/LabOrderStatus.java`
- `lab-service/src/main/java/com/hospital/lab/model/LabResult.java`
- `lab-service/src/main/java/com/hospital/lab/model/OrderPriority.java`
- `lab-service/src/main/java/com/hospital/lab/model/ReferenceRange.java`
- `lab-service/src/main/java/com/hospital/lab/model/ResultAmendment.java`
- `lab-service/src/main/java/com/hospital/lab/model/ResultFlag.java`
- `lab-service/src/main/java/com/hospital/lab/model/TestDefinition.java`
- `lab-service/src/main/java/com/hospital/lab/model/TestParameter.java`
- `lab-service/src/main/java/com/hospital/lab/model/ValueType.java`
- `lab-service/src/main/java/com/hospital/lab/repository/LabAttachmentRepository.java`
- `lab-service/src/main/java/com/hospital/lab/repository/ResultAmendmentRepository.java`
- `lab-service/src/main/java/com/hospital/lab/repository/TestDefinitionRepository.java`
- `lab-service/src/main/java/com/hospital/lab/security/AuthTokenFilter.java`
- `lab-service/src/main/java/com/hospital/lab/security/CurrentUser.java`
- `lab-service/src/main/java/com/hospital/lab/security/JwtUtils.java`
- `lab-service/src/main/java/com/hospital/lab/security/StaffPrincipal.java`
- `lab-service/src/main/java/com/hospital/lab/service/AttachmentService.java`
- `lab-service/src/main/java/com/hospital/lab/service/AttachmentStorage.java`
- `lab-service/src/main/java/com/hospital/lab/service/CatalogService.java`
- `lab-service/src/main/java/com/hospital/lab/service/LabMapper.java`
- `lab-service/src/main/java/com/hospital/lab/service/LabOrderService.java`
- `lab-service/src/main/java/com/hospital/lab/service/OrderNumberService.java`
- `lab-service/src/main/java/com/hospital/lab/service/ReferenceRangeResolver.java`
- `lab-service/src/main/java/com/hospital/lab/service/ReportService.java`
- `lab-service/src/main/java/com/hospital/lab/service/ResolvedRange.java`
- `lab-service/src/main/java/com/hospital/lab/service/ResultFlagEvaluator.java`
- `lab-service/src/main/resources/db/migration/V1__lab_initial_schema.sql`
- `lab-service/src/main/resources/db/migration/V2__seed_test_catalog.sql`
- `lab-service/src/test/java/com/hospital/lab/service/AttachmentSniffTest.java`
- `lab-service/src/test/java/com/hospital/lab/service/LabResultEntryTest.java`
- `lab-service/src/test/java/com/hospital/lab/service/ReferenceRangeResolverTest.java`
- `lab-service/src/test/java/com/hospital/lab/service/ResultFlagEvaluatorTest.java`

**opd-service**

- `opd-service/src/main/java/com/hospital/opd/client/LabClient.java`
- `opd-service/src/main/java/com/hospital/opd/client/LabOrderCreated.java`
- `opd-service/src/main/java/com/hospital/opd/client/LabOrderPayload.java`
- `opd-service/src/main/java/com/hospital/opd/config/AppConfig.java`
- `opd-service/src/main/java/com/hospital/opd/controller/VisitController.java`
- `opd-service/src/main/java/com/hospital/opd/dto/CancelRequest.java`
- `opd-service/src/main/java/com/hospital/opd/dto/ConsultationNotesRequest.java`
- `opd-service/src/main/java/com/hospital/opd/dto/LabOrderRequest.java`
- `opd-service/src/main/java/com/hospital/opd/dto/LabResultsReadyRequest.java`
- `opd-service/src/main/java/com/hospital/opd/dto/PatientBrief.java`
- `opd-service/src/main/java/com/hospital/opd/dto/PatientRequest.java`
- `opd-service/src/main/java/com/hospital/opd/dto/PatientResponse.java`
- `opd-service/src/main/java/com/hospital/opd/dto/TriageRequest.java`
- `opd-service/src/main/java/com/hospital/opd/dto/VisitCreateRequest.java`
- `opd-service/src/main/java/com/hospital/opd/dto/VisitDetailResponse.java`
- `opd-service/src/main/java/com/hospital/opd/dto/VisitEventResponse.java`
- `opd-service/src/main/java/com/hospital/opd/dto/VisitSummaryResponse.java`
- `opd-service/src/main/java/com/hospital/opd/dto/VitalsResponse.java`
- `opd-service/src/main/java/com/hospital/opd/exception/ApiError.java`
- `opd-service/src/main/java/com/hospital/opd/exception/BusinessRuleException.java`
- `opd-service/src/main/java/com/hospital/opd/exception/DownstreamServiceException.java`
- `opd-service/src/main/java/com/hospital/opd/exception/GlobalExceptionHandler.java`
- `opd-service/src/main/java/com/hospital/opd/exception/NotFoundException.java`
- `opd-service/src/main/java/com/hospital/opd/model/Gender.java`
- `opd-service/src/main/java/com/hospital/opd/model/TriagePriority.java`
- `opd-service/src/main/java/com/hospital/opd/model/Visit.java`
- `opd-service/src/main/java/com/hospital/opd/model/VisitEvent.java`
- `opd-service/src/main/java/com/hospital/opd/model/VisitStatus.java`
- `opd-service/src/main/java/com/hospital/opd/model/VitalFlag.java`
- `opd-service/src/main/java/com/hospital/opd/model/VitalSigns.java`
- `opd-service/src/main/java/com/hospital/opd/repository/VisitEventRepository.java`
- `opd-service/src/main/java/com/hospital/opd/repository/VisitRepository.java`
- `opd-service/src/main/java/com/hospital/opd/repository/VitalSignsRepository.java`
- `opd-service/src/main/java/com/hospital/opd/security/AuthTokenFilter.java`
- `opd-service/src/main/java/com/hospital/opd/security/CurrentUser.java`
- `opd-service/src/main/java/com/hospital/opd/security/JwtUtils.java`
- `opd-service/src/main/java/com/hospital/opd/security/StaffPrincipal.java`
- `opd-service/src/main/java/com/hospital/opd/service/AgeCalculator.java`
- `opd-service/src/main/java/com/hospital/opd/service/OpdMapper.java`
- `opd-service/src/main/java/com/hospital/opd/service/PatientService.java`
- `opd-service/src/main/java/com/hospital/opd/service/SequenceService.java`
- `opd-service/src/main/java/com/hospital/opd/service/VisitService.java`
- `opd-service/src/main/java/com/hospital/opd/service/VitalsAssessor.java`
- `opd-service/src/main/resources/db/migration/V1__opd_initial_schema.sql`
- `opd-service/src/test/java/com/hospital/opd/model/VisitStatusTest.java`
- `opd-service/src/test/java/com/hospital/opd/service/AgeCalculatorTest.java`
- `opd-service/src/test/java/com/hospital/opd/service/VitalsAssessorTest.java`

**tools**

- `tools/gen_lab_catalog.py`

## Updated files (33)


**Testing.md**

- `Testing.md`

**auth-service**

- `auth-service/pom.xml`
- `auth-service/src/main/java/com/hospital/auth/config/DataInitializer.java`
- `auth-service/src/main/java/com/hospital/auth/config/SecurityConfig.java`
- `auth-service/src/main/java/com/hospital/auth/controller/AuthController.java`
- `auth-service/src/main/java/com/hospital/auth/jwt/JwtUtils.java`
- `auth-service/src/main/java/com/hospital/auth/model/Role.java`
- `auth-service/src/main/resources/application.yml`

**docker-compose.yml**

- `docker-compose.yml`

**frontend-ui**

- `frontend-ui/Dockerfile`
- `frontend-ui/README.md`
- `frontend-ui/src/App.jsx`
- `frontend-ui/src/api/axiosClient.js`
- `frontend-ui/src/components/layout/AppShell.jsx`
- `frontend-ui/src/components/ui/Icon.jsx`
- `frontend-ui/src/components/ui/index.jsx`
- `frontend-ui/src/context/AuthContext.jsx`
- `frontend-ui/src/modules/auth/Login.jsx`
- `frontend-ui/src/modules/opd/DoctorDashboard.jsx`
- `frontend-ui/src/modules/opd/NurseDashboard.jsx`
- `frontend-ui/src/styles/global.css`

**lab-service**

- `lab-service/pom.xml`
- `lab-service/src/main/java/com/hospital/lab/config/SecurityConfig.java`
- `lab-service/src/main/java/com/hospital/lab/controller/LabOrderController.java`
- `lab-service/src/main/java/com/hospital/lab/model/LabOrder.java`
- `lab-service/src/main/java/com/hospital/lab/repository/LabOrderRepository.java`
- `lab-service/src/main/resources/application.yml`

**opd-service**

- `opd-service/pom.xml`
- `opd-service/src/main/java/com/hospital/opd/config/SecurityConfig.java`
- `opd-service/src/main/java/com/hospital/opd/controller/PatientController.java`
- `opd-service/src/main/java/com/hospital/opd/model/Patient.java`
- `opd-service/src/main/java/com/hospital/opd/repository/PatientRepository.java`
- `opd-service/src/main/resources/application.yml`
