# Budget Request Management System

## Overview
This Grails 5.x application implements a budget request management system that allows users to submit budget requests for their department and managers to approve or reject those requests. The system includes comprehensive audit logging to track all changes and enforce business rules.

## Key Features
- **User Submission**: Users can submit budget requests with justification
- **Manager Approval**: Managers can approve or reject requests with notes
- **Audit Logging**: Complete trail of all actions (creation, approval, rejection)
- **Validation Rules**: Budget constraints and duplicate prevention
- **REST API**: JSON-based endpoints for all operations

## Project Structure
```
budget-app/                           <-- root
├── grails-app/
│   ├── controllers/
│   │   └── com/pyramidion/budget/
│   │       ├── AuditLogController.groovy
│   │       └── BudgetRequestController.groovy
│   ├── domain/
│   │   └── com/pyramidion/budget/
│   │       ├── Department.groovy
│   │       ├── BudgetRequest.groovy
│   │       ├── AuditLog.groovy
│   │       └── User.groovy
│   ├── services/
│   │   └── com/pyramidion/budget/
│   │       ├── BudgetRequestService.java
│   │       └── AuditLogService.java
│   └── conf/
│       └── application.yml
├── src/test/groovy/
│   └── com/pyramidion/budget/
│       └── BudgetRequestServiceSpec.groovy
├── build.gradle
├── settings.gradle
└── README.md
```

## Domain Models

### 1. User
Represents a system user with role-based access.

**Fields:**
- `username` - Unique identifier (String)
- `displayName` - displayName (String, unique)
- `role` - User role: `USER` or `MANAGER` (String)

**Role Logic:**
- **USER**: Can submit budget requests and view own requests
- **MANAGER**: Can approve/reject requests and view all requests
- **ADMIN**: admin user

### 2. Department
Represents an organizational department with budget allocation.

**Fields:**
- `name` - Department name (String, unique)
- `code` - Short code identifier (String, unique)
- `currentBudget` - Available budget amount (BigDecimal)
- `yearlyAllocation` - Total yearly budget (BigDecimal)
- `location` - Department location (String, optional)
- `dateCreated`, `lastUpdated` - Timestamps (Date, auto-managed)

**Business Logic:**
- `currentBudget` decreases when requests are approved
- Maximum request limit is 10% of `yearlyAllocation`

### 3. BudgetRequest
Represents a budget request submitted by a user.

**Fields:**
- `requestedAmount` - Amount requested (BigDecimal, > 0)
- `purpose` - Purpose of the request (String, 5-200 chars)
- `status` - Request status: `PENDING`, `APPROVED`, `REJECTED` (String)
- `requestedBy` - Username who submitted (String)
- `approvedBy` - Username who approved/rejected (String, optional)
- `notes` - Detailed justification (String, 10-1000 chars)
- `department` - Associated department (Department)
- `dateCreated`, `lastUpdated` - Timestamps (Date, auto-managed)

**Validation Rules:**
- Amount must be positive
- Cannot exceed 10% of department's yearly allocation
- No duplicate purpose within 7 days for same department
- Justification is required

### 4. AuditLog
Tracks all actions performed on budget requests for compliance.

**Fields:**
- `action` - Action performed: `INSERT`, `APPROVE`, `REJECT` (String)
- `entityId` - ID of the affected entity (Long)
- `entityType` - Type of entity: `BudgetRequest`, `Department` (String)
- `oldValue` - Previous state (String, optional)
- `newValue` - New state (String, optional)
- `changedBy` - Username performing action (String)
- `timestamp` - When action occurred (Date)

## Service Layer

### BudgetRequestService
Handles all business logic for budget requests with transaction management.

**Main Methods:**
- `submitRequest(requestData)` - Creates new PENDING request
- `approveRequest(requestId, manager, reason)` - Approves request and updates budget
- `rejectRequest(requestId, manager, reason)` - Rejects request with reason
- `listPendingRequests()` - Returns all PENDING requests
- `getRequestById(requestId)` - Returns user's requests by Id

**Validation:**
- Validates request amounts against department limits
- Checks for duplicate purposes within timeframe
- Ensures sufficient department budget for approvals

### AuditLogService
Manages audit logging for all system actions.

**Methods:**
- `logAction(action, entityId, entityType, oldValue, newValue, changedBy)` - Creates audit entry
- `getEntityAuditLogs(entityType, entityId)` - Gets audit trail for specific entity

## REST API Endpoints

### Budget Request Operations
- `POST /api/budget-request` - Submit new budget request
- `PUT /api/budget-request/{id}/approve` - Approve request (Manager only)
- `PUT /api/budget-request/{id}/reject` - Reject request (Manager only)
- `GET /api/budget-request/pending` - List pending requests (Manager only)
- `GET /api/budget-request` - List requests (filtered by role)
- `GET /api/budget-request/{id}` - View specific request

### Audit Operations
- `GET /api/audit-logs?entityType=BudgetRequest` - Filter by entity type

## Configuration

### Database Setup
The application uses different datasources per environment:

- **Development**: H2 in-memory database
- **Test**: H2 in-memory database
- **Production**: MySQL database

### Application Configuration (`application.yml`)
```yaml
grails:
    profile: rest-api
    codegen:
        defaultPackage: com.pyramidion.budget

environments:
    development:
        dataSource:
            dbCreate: create-drop
            url: jdbc:h2:mem:devDb
            driverClassName: org.h2.Driver
            username: sa
            password: ''
```

## Getting Started

### Prerequisites
- Java 11 or higher
- Grails 5.x
- MySQL (for production)

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd budget-approval-system
   ```

2. **Run the application**
   ```bash
   grails run-app
   ```
   or
   ```bash
   ./gradlew bootRun
   ```

3. **Access the application**
    - API Base URL: `http://localhost:8080/api`
    - H2 Console (dev): `http://localhost:8080/h2-console`

### Initial Setup
Create test users and departments in your bootstrap or via console:

```groovy
// Create users
new User(username: 'user', displayName: 'User', role: 'USER').save()
new User(username: 'manager', displayName: 'Manager', role: 'MANAGER').save()

// Create department
new Department(name: 'IT', code: 'IT01', currentBudget: 100000, yearlyAllocation: 200000).save()
```

## API Usage Examples

### Submit Budget Request
```bash
POST /api/budget-request
Content-Type: application/json

{
  "requestedById": 1,
  "departmentId": 1,
  "requestedAmount": 5000.00,
  "purpose": "New development laptops",
  "notes": "Current laptops are 5 years old and affecting productivity"
}
```

### Approve Request (Manager Only)
```bash
PUT /api/budget-request/123/approve
Content-Type: application/json

{
  "managerId": 2,
  "notes": "Approved"
}
```

### Reject Request (Manager Only)
```bash
PUT /api/budget-request/123/reject
Content-Type: application/json

{
  "managerId": 2,
  "notes": "Budget constraints this quarter"
}
```

### List Pending Requests (Manager Only)
```bash
GET /api/budget-request/pending
```

### View Audit Logs (Manager Only)
```bash
GET /api/audit-logs?entityType=BudgetRequest
```

## Business Rules

### Validation Rules
1. **Positive Amount**: All requests must have amount > 0
2. **10% Limit**: Requests cannot exceed 10% of department's yearly allocation
3. **Duplicate Prevention**: Same purpose cannot be requested within 7 days for same department
4. **Budget Availability**: Department must have sufficient budget for approval

### Authorization Rules
1. **User Role**: Can submit requests and view own requests only
2. **Manager Role**: Can approve/reject any request and view all requests

### Workflow Rules
1. **New Requests**: Always created with `PENDING` status
2. **Approval**: Only pending requests can be approved; deducts from department budget
3. **Rejection**: Only pending requests can be rejected
4. **Audit Trail**: All state changes are logged automatically

## Testing

### Run Tests
```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests BudgetRequestServiceSpec
```

### Test Coverage
- Unit tests for service layer business logic
- Validation tests for domain constraints