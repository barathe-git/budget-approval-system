package com.pyramidion.budget

import grails.testing.gorm.DomainUnitTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class BudgetRequestServiceSpec extends Specification implements ServiceUnitTest<BudgetRequestService>, DomainUnitTest<BudgetRequest> {

    AuditLogService auditLogServiceMock
    Department dept
    User requester
    User manager

    def setup() {
        // Mock AuditLogService
        auditLogServiceMock = Mock(AuditLogService)
        service.auditLogService = auditLogServiceMock

        // Create and mock Department
        dept = new Department(
                name: "IT",
                location: "Head Office",
                currentBudget: 50000,
                yearlyAllocation: 100000
        )
        mockDomain(Department, [dept])

        // Create and mock Users
        requester = new User(
                username: "barath",
                role: User.Role.USER,
                displayName: "Barath Elumalai"
        )
        manager = new User(
                username: "manager",
                role: User.Role.MANAGER,
                displayName: "Manager One"
        )
        mockDomain(User, [requester, manager])
    }

    def "submitRequest should save request and log audit"() {
        given:
        BudgetRequest request = new BudgetRequest(
                requestedAmount: 5000,
                purpose: "Purchase laptops",
                department: dept,
                requestedBy: requester,
                notes: "Urgent requirement"
        )
        mockDomain(BudgetRequest, [])

        def savedRequest

        when:
        savedRequest = service.submitRequest(request)

        then:
        savedRequest != null
        savedRequest.id != null
        savedRequest.status == BudgetRequest.Status.PENDING
        1 * auditLogServiceMock.logAction(
                "CREATED",
                { it instanceof Long },
                "BudgetRequest",
                null,
                _,
                "barath"
        )
    }

    def "approveRequest should approve valid request"() {
        given:
        BudgetRequest request = new BudgetRequest(
                requestedAmount: 5000,
                purpose: "Purchase laptops",
                department: dept,
                requestedBy: requester
        )
        mockDomain(BudgetRequest, [request])

        def approved

        when:
        approved = service.approveRequest(request.id, manager, "Approved")

        then:
        approved != null
        approved.id != null
        approved.status == BudgetRequest.Status.APPROVED
        approved.approvedBy.id == manager.id
        dept.currentBudget == 45000
        1 * auditLogServiceMock.logAction(
                "APPROVED",
                { it instanceof Long },
                "BudgetRequest",
                _,
                _,
                "manager"
        )
    }

    def "rejectRequest should reject valid request"() {
        given:
        BudgetRequest request = new BudgetRequest(
                requestedAmount: 5000,
                purpose: "Purchase laptops",
                department: dept,
                requestedBy: requester
        )
        mockDomain(BudgetRequest, [request])

        def rejected

        when:
        rejected = service.rejectRequest(request.id, manager, "Rejected")

        then:
        rejected != null
        rejected.id != null
        rejected.status == BudgetRequest.Status.REJECTED
        rejected.approvedBy.id == manager.id
        1 * auditLogServiceMock.logAction(
                "REJECTED",
                { it instanceof Long },
                "BudgetRequest",
                _,
                _,
                "manager"
        )
    }
}
