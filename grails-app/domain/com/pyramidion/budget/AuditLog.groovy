package com.pyramidion.budget

import grails.gorm.annotation.Entity
import java.time.LocalDateTime

@Entity
class  AuditLog {

    String action
    Long entityId
    String entityType
    String oldValue
    String newValue
    String changedBy
    LocalDateTime timestamp

    static mapping = {
        oldValue type: 'text'
        newValue type: 'text'
    }

    static constraints = {
        oldValue nullable: true
        newValue nullable: true
        changedBy nullable: true
        timestamp nullable: false
    }

    def beforeInsert() {
        if (!timestamp) timestamp = LocalDateTime.now()
    }

    Map toMap() {
        return [
                id        : id,
                action    : action,
                entityId  : entityId,
                entityType: entityType,
                oldValue  : oldValue,
                newValue  : newValue,
                changedBy : changedBy,
                timestamp : timestamp.toString()
        ]
    }
}
