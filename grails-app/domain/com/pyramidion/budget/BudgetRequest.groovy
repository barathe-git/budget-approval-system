package com.pyramidion.budget

import grails.gorm.annotation.Entity
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
class BudgetRequest {

    enum Status { PENDING, APPROVED, REJECTED }

    BigDecimal requestedAmount
    String purpose
    Status status = Status.PENDING

    User requestedBy
    User approvedBy
    Department department
    String notes

    LocalDateTime dateCreated
    LocalDateTime lastUpdated

    static constraints = {
        requestedAmount nullable: false, min: 0.01G
        purpose nullable: false, maxSize: 1000
        status nullable: false
        requestedBy nullable: false
        approvedBy nullable: true
        department nullable: false
        notes nullable: true, maxSize: 1000
    }

    static belongsTo = [department: Department]

    String toString() {
        "BudgetRequest(id=${id}, amount=${requestedAmount}, purpose=${purpose}, status=${status}, requestedBy=${requestedBy}, " +
                "approvedBy=${approvedBy}, department=${department}, notes=${notes})"
    }
}
