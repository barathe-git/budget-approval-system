package com.pyramidion.budget

import grails.gorm.transactions.Transactional
import java.time.LocalDateTime

@Transactional
class AuditLogService {

    /**
     * Create audit logs
     */
    void logAction(String action, Long entityId, String entityType, String oldValue, String newValue, String changedBy) {
        AuditLog log = new AuditLog(
                action: action,
                entityId: entityId,
                entityType: entityType,
                oldValue: oldValue,
                newValue: newValue,
                changedBy: changedBy,
                timestamp: LocalDateTime.now()
        )
        log.save(flush: true, failOnError: true)
    }
}
