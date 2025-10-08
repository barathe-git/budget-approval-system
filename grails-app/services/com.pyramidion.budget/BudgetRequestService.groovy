package com.pyramidion.budget

import grails.converters.JSON
import org.springframework.stereotype.Service
import grails.gorm.transactions.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import groovy.json.JsonOutput

@Service
@Transactional
class BudgetRequestService {

    AuditLogService auditLogService

    BudgetRequest submitRequest(BudgetRequest request) {
        validateRequest(request)
        request.status = BudgetRequest.Status.PENDING
        request.notes = request.notes ?: ""
        request.save(flush: true, failOnError: true)
        logAudit("CREATED", request, null, request.requestedBy?.username)
        return request
    }

    BudgetRequest approveRequest(Long requestId, User manager, String notes) {
        processStatusChange(requestId, manager, notes, BudgetRequest.Status.APPROVED)
    }

    BudgetRequest rejectRequest(Long requestId, User manager, String notes) {
        processStatusChange(requestId, manager, notes, BudgetRequest.Status.REJECTED)
    }

    List<BudgetRequest> listPendingRequests() {
        BudgetRequest.findAllByStatus(BudgetRequest.Status.PENDING) ?: []
    }

    // Private Helpers

    private BudgetRequest fetchRequest(Long id) {
        BudgetRequest request = BudgetRequest.get(id)
        if (!request) throw new IllegalArgumentException("Request not found")
        return request
    }

    private void validateManager(User manager) {
        if (!manager || manager.role != User.Role.MANAGER)
            throw new SecurityException("Only managers can perform this action")
    }

    private void validateRequest(BudgetRequest request) {
        if (!request.requestedAmount || request.requestedAmount <= 0)
            throw new IllegalArgumentException("Requested amount must be positive")
        if (!request.department)
            throw new IllegalArgumentException("Department must be set")
        if (!request.requestedBy)
            throw new IllegalArgumentException("RequestedBy must be set")

        BigDecimal maxAllowed = request.department.yearlyAllocation?.multiply(0.10G) ?: 0
        if (request.requestedAmount > maxAllowed)
            throw new IllegalArgumentException("Request exceeds 10% of department’s yearly allocation")

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS)
        def duplicate = BudgetRequest.createCriteria().list {
            eq("purpose", request.purpose)
            eq("requestedBy", request.requestedBy)
            gt("dateCreated", sevenDaysAgo)
        }
        if (duplicate)
            throw new IllegalArgumentException("Duplicate request for same purpose within 7 days")
    }

    private void validateApproval(BudgetRequest request) {
        if (request.status == BudgetRequest.Status.APPROVED)
            throw new IllegalStateException("Request is already approved")
        if (request.status == BudgetRequest.Status.REJECTED)
            throw new IllegalStateException("Request has been rejected and cannot be approved")
        if (!request.department)
            throw new IllegalArgumentException("Department not set on request")

        BigDecimal maxAllowed = request.department.yearlyAllocation?.multiply(0.10G) ?: 0
        if (request.requestedAmount > maxAllowed)
            throw new IllegalArgumentException("Request exceeds 10% of department’s yearly allocation")
        if (request.department.currentBudget - request.requestedAmount < 0)
            throw new IllegalArgumentException("Insufficient department budget")
    }

    private void validateRejection(BudgetRequest request) {
        if (request.status == BudgetRequest.Status.REJECTED)
            throw new IllegalStateException("Request is already rejected")
        if (request.status == BudgetRequest.Status.APPROVED)
            throw new IllegalStateException("Request has been approved and cannot be rejected")
    }

    private BudgetRequest processStatusChange(Long requestId, User manager, String notes, BudgetRequest.Status targetStatus) {
        BudgetRequest request = fetchRequest(requestId)
        validateManager(manager)

        if (targetStatus == BudgetRequest.Status.APPROVED) {
            validateApproval(request)
        } else {
            validateRejection(request)
        }

        def oldJson = JsonOutput.toJson(BudgetUtils.simplifiedRequestMap(request))

        // Update status & budget
        request.status = targetStatus
        request.approvedBy = manager
        request.notes = notes ?: ""
        if (targetStatus == BudgetRequest.Status.APPROVED) {
            request.department.currentBudget -= request.requestedAmount
            request.department.save(flush: true, failOnError: true)
        }

        request.save(flush: true, failOnError: true)

        def newJson = JsonOutput.toJson(BudgetUtils.simplifiedRequestMap(request))
        logAudit(targetStatus.toString(), request, oldJson, manager.username)

        return request
    }

    private void logAudit(String action, BudgetRequest request, String oldJson, String changedBy) {
        def newJson = JsonOutput.toJson(BudgetUtils.simplifiedRequestMap(request))
        auditLogService.logAction(action, request.id, "BudgetRequest", oldJson, newJson, changedBy)
    }
}
