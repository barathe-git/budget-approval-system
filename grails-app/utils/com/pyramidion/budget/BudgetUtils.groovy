package com.pyramidion.budget

class BudgetUtils {

    public static Map simplifiedRequestMap(BudgetRequest request) {
        return [
                id             : request.id,
                requestedAmount: request.requestedAmount,
                purpose        : request.purpose,
                status         : request.status.toString(),
                requestedBy    : request.requestedBy ? [id: request.requestedBy.id, username: request.requestedBy.username, role: request.requestedBy.role] : null,
                approvedBy     : request.approvedBy ? [id: request.approvedBy.id, username: request.approvedBy.username, role: request.approvedBy.role] : null,
                department     : request.department ? [id: request.department.id, name: request.department.name, location: request.department.location, currentBudget: request.department.currentBudget] : null,
                notes          : request.notes,
                dateCreated    : request.dateCreated?.toString(),
                lastUpdated    : request.lastUpdated?.toString()
        ]
    }
}
