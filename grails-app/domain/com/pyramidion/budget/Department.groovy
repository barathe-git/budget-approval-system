package com.pyramidion.budget

import grails.gorm.annotation.Entity
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
class Department {

    String name
    BigDecimal currentBudget = 0.0G
    BigDecimal yearlyAllocation = 0.0G
    String location
    String code

    LocalDateTime dateCreated
    LocalDateTime lastUpdated

    static constraints = {
        name nullable: false, unique: true, maxSize: 150
        code nullable: true, unique: true, maxSize: 50
        location nullable: true, maxSize: 255
        currentBudget nullable: false, min: 0.0G
        yearlyAllocation nullable: false, min: 0.0G
    }

    String toString() {
        "Department(id=${id}, name=${name}, location=${location}, currentBudget=${currentBudget})"
    }
}
