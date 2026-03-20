# Feature Spec: 1-customer-onboarding-kyc

## Description
Customer Onboarding with KYC in a Spring Boot fintech banking system. This feature enables new customers to register, submit KYC documents, and have their identity verified before account creation. Includes API endpoints, request/response objects, validation rules, KYC verification logic, failure scenarios, database schema (Customer, Account, KYC), edge cases, and security considerations.

## Created
2026-03-20

## Actors
- Customer (applicant)
- System (backend)
- KYC verification provider (external or internal)
- Bank admin (optional, for manual review)

## User Scenarios & Testing
1. Customer submits onboarding form with personal details and KYC documents.
2. System validates input and initiates KYC verification.
3. If KYC passes, account is created and customer receives confirmation.
4. If KYC fails, customer receives rejection with reason.
5. Admin reviews flagged/rejected cases (if manual review enabled).

## Functional Requirements
1. Provide API endpoint for customer onboarding (POST /api/customers/onboard).
2. Accept personal details and KYC documents in request.
3. Validate required fields (name, DOB, address, ID type/number, document images).
4. Validate document formats (PDF, JPEG, PNG; max size 5MB per file).
5. Initiate KYC verification (automated or manual).
6. Respond with onboarding status (success, pending, failed) and reason if failed.
7. Store customer, account, and KYC data in database.
8. Prevent duplicate onboarding (unique ID number per customer).
9. Log all onboarding attempts and outcomes.
10. Secure sensitive data (encryption, access control).

## API Endpoints
- POST /api/customers/onboard
  - Request: Customer details, KYC documents
  - Response: Onboarding status, customer/account IDs, failure reason if any
- GET /api/customers/{id}/kyc-status
  - Request: Customer ID
  - Response: KYC status, details

## Request/Response Objects
### Onboarding Request
- name (string, required)
- dateOfBirth (string, required, ISO date)
- address (string, required)
- idType (enum: Passport, NationalID, DriverLicense, required)
- idNumber (string, required)
- kycDocuments (array of files, required)

### Onboarding Response
- status (enum: success, pending, failed)
- customerId (string, if success)
- accountId (string, if success)
- failureReason (string, if failed)

### KYC Status Response
- status (enum: pending, verified, failed)
- details (string)

## Validation Rules
- All fields required
- dateOfBirth must be valid date, applicant >= 18 years
- idType must be valid enum
- idNumber must be unique
- kycDocuments: min 1, max 3 files; allowed formats PDF/JPEG/PNG; max size 5MB each

## KYC Verification Logic
- Automated verification: match ID number and name against trusted sources
- Document image quality check
- Fraud detection (duplicate, tampered docs)
- Manual review for flagged cases
- Status transitions: pending → verified/failed

## Failure Scenarios
- Invalid input (missing/incorrect fields)
- Document upload errors (format, size)
- KYC verification failed (mismatch, fraud, expired docs)
- Duplicate onboarding attempt
- System errors (timeout, provider unavailable)

## Database Schema
### Customer
- id (PK, string)
- name (string)
- dateOfBirth (date)
- address (string)
- idType (string)
- idNumber (string, unique)
- createdAt (datetime)

### Account
- id (PK, string)
- customerId (FK)
- accountNumber (string, unique)
- accountType (string)
- balance (decimal)
- createdAt (datetime)

### KYC
- id (PK, string)
- customerId (FK)
- status (enum: pending, verified, failed)
- submittedAt (datetime)
- verifiedAt (datetime, nullable)
- failureReason (string, nullable)
- documents (array/file references)

## Edge Cases
- Applicant under 18 years
- Multiple onboarding attempts with same ID
- Corrupted or unreadable document files
- KYC provider unavailable
- Manual review required for ambiguous cases

## Security Considerations
- Encrypt sensitive fields (ID number, documents)
- Access control: only authorized staff/admins can view KYC data
- Audit logging for all onboarding and KYC actions
- Secure file storage for documents
- Rate limiting to prevent abuse

## Assumptions
- KYC provider integration is available
- Manual review is enabled for flagged cases
- Standard document formats and size limits
- Customer must be at least 18 years old

## Success Criteria
- 95% of onboarding requests processed within 2 minutes
- 100% of valid customers have verified KYC before account creation
- 99% of document uploads succeed without errors
- No duplicate customers created
- Sensitive data is protected and only accessible to authorized users
- All failure scenarios are handled gracefully with clear user feedback

## Key Entities
- Customer
- Account
- KYC

## Constraints
- Unique ID number per customer
- Minimum age 18
- Document format and size restrictions
- KYC must be verified before account activation
