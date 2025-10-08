package com.pyramidion.budget

import grails.rest.RestfulController
import grails.converters.JSON

class AuditLogController extends RestfulController<AuditLog> {

    static responseFormats = ['json']
    static allowedMethods = [index: "GET"]

    AuditLogController() {
        super(AuditLog)
    }

    /**
     * GET /api/audit-logs?entityType=BudgetRequest
     */
    def index() {
        try {
            String entityType = params.entityType
            if (!entityType) {
                render(status: 400, text: [error: "entityType parameter is required"] as JSON)
                return
            }

            List<AuditLog> logs = AuditLog.findAllByEntityType(entityType, [sort: "timestamp", order: "desc"])
            render logs.collect { log ->
                [
                        id: log.id,
                        action: log.action,
                        entityId: log.entityId,
                        entityType: log.entityType,
                        oldValue: log.oldValue ? JSON.parse(log.oldValue) : null,
                        newValue: JSON.parse(log.newValue),
                        changedBy: log.changedBy,
                        timestamp: log.timestamp.toString()
                ]
            } as JSON
        } catch (Exception e) {
            render(status: 500, text: [error: e.message] as JSON)
        }
    }
}
