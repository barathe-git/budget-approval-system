package com.pyramidion.budget

import grails.rest.RestfulController
import grails.converters.JSON
import java.math.BigDecimal

class BudgetRequestController extends RestfulController<BudgetRequest> {

    static responseFormats = ['json']
    static allowedMethods = [
            create   : "POST",
            approve: "PUT",
            reject : "PUT",
            pending: "GET",
            show   : "GET",
            index  : "GET"
    ]

    BudgetRequestService budgetRequestService

    BudgetRequestController() {
        super(BudgetRequest)
    }

    /**
     * POST /api/budget-request
     */
    def create() {
        handleRequest {
            def body = request.JSON
            User user = fetchUser(body.requestedById ?: params.requestedById)
            Department dept = fetchDepartment(body.departmentId ?: params.departmentId)
            BigDecimal amount = parseAmount(body.requestedAmount ?: params.requestedAmount)

            BudgetRequest requestModel = new BudgetRequest(
                    requestedAmount: amount,
                    purpose       : body.purpose ?: params.purpose,
                    requestedBy   : user,
                    department    : dept,
                    notes         : body.notes ?: params.notes
            )

            budgetRequestService.submitRequest(requestModel)
        }
    }

    /**
     * GET /api/budget-request/{id}
     */
    def show(Long id) {
        handleRequest {
            if (!id) throw new IllegalArgumentException("Request ID is required")
            BudgetRequest request = BudgetRequest.get(id)
            if (!request) throw new IllegalArgumentException("BudgetRequest not found with id: $id")
            return request
        }
    }

    /**
     * PUT /api/budget-request/{id}/approve
     */
    def approve(Long id) {
        handleRequest {
            def body = request.JSON
            User manager = fetchUser(body.managerId ?: params.managerId)
            String notes = body.notes ?: params.notes ?: ""
            budgetRequestService.approveRequest(id, manager, notes)
        }
    }

    /**
     * PUT /api/budget-request/{id}/reject
     */
    def reject(Long id) {
        handleRequest {
            def body = request.JSON
            User manager = fetchUser(body.managerId ?: params.managerId)
            String notes = body.notes ?: params.notes ?: ""
            budgetRequestService.rejectRequest(id, manager, notes)
        }
    }

    /**
     * GET /api/budget-request/pending
     */
    def pending() {
        handleRequest {
            budgetRequestService.listPendingRequests()
        }
    }

    // Helper methods

    /**
     * Common wrapper to handle exceptions and respond consistently
     */
    private void handleRequest(Closure action) {
        try {
            def result = action.call()
            respond result, status: (result instanceof BudgetRequest ? 201 : 200)
        } catch (IllegalArgumentException e) {
            renderError(e.message, 400)
        } catch (SecurityException e) {
            renderError(e.message, 403)
        } catch (IllegalStateException e) {
            renderError(e.message, 409)
        } catch (Exception e) {
            renderError(e.message, 500)
        }
    }

    private User fetchUser(Object idObj) {
        Long id = idObj?.toLong()
        if (!id) throw new IllegalArgumentException("User ID is required")
        User user = User.get(id)
        if (!user) throw new IllegalArgumentException("User not found with id: $id")
        return user
    }

    private Department fetchDepartment(Object idObj) {
        Long id = idObj?.toLong()
        if (!id) throw new IllegalArgumentException("Department ID is required")
        Department dept = Department.get(id)
        if (!dept) throw new IllegalArgumentException("Department not found with id: $id")
        return dept
    }

    private BigDecimal parseAmount(Object amountObj) {
        if (!amountObj) throw new IllegalArgumentException("requestedAmount is required")
        try {
            return new BigDecimal(amountObj.toString())
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid requestedAmount: $amountObj")
        }
    }

    private void renderError(String message, int statusCode) {
        render(status: statusCode, text: [error: message] as JSON)
    }
}
