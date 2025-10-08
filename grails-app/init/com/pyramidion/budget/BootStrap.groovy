package com.pyramidion.budget

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import groovy.json.JsonOutput

@Transactional
class BootStrap {

    AuditLogService auditLogService
    BudgetRequestService budgetRequestService

    def init = { servletContext ->

        User.withTransaction { status ->
            if (!User.count()) {
                new User(username: 'admin', displayName: 'Admin User', role: User.Role.ADMIN).save(flush: true)
                new User(username: 'manager', displayName: 'Product Manager', role: User.Role.MANAGER).save(flush: true)
                new User(username: 'barath', displayName: 'Barath Elumalai', role: User.Role.USER).save(flush: true)
                println "Default users created: ${User.count()}"
            }
        }

        Department.withTransaction { status ->
            if (!Department.count()) {
                new Department(
                        name: 'IT',
                        currentBudget: new BigDecimal('50000'),
                        yearlyAllocation: new BigDecimal('100000'),
                        location: 'Head Office',
                        code: 'IT01'
                ).save(flush: true)

                new Department(
                        name: 'HR',
                        currentBudget: new BigDecimal('30000'),
                        yearlyAllocation: new BigDecimal('60000'),
                        location: 'Head Office',
                        code: 'HR01'
                ).save(flush: true)
                println "Default departments created: ${Department.count()}"
            }
        }

        BudgetRequest.withTransaction { status ->
            if (!BudgetRequest.count()) {
                def user = User.findByUsername('barath')
                def itDept = Department.findByName('IT')

                def request = new BudgetRequest(
                        requestedAmount: new BigDecimal('5000'),
                        purpose: 'Purchase laptops',
                        requestedBy: user,
                        department: itDept,
                        notes: 'Urgent requirement',
                        dateCreated: LocalDateTime.now(),
                        lastUpdated: LocalDateTime.now()
                ).save(flush: true)

                def newJson = JsonOutput.toJson(BudgetUtils.simplifiedRequestMap(request))

                // Make sure auditLogService is available
                if (auditLogService) {
                    auditLogService.logAction(
                            'CREATED',
                            request.id,
                            'BudgetRequest',
                            null,
                            newJson,
                            user.username
                    )
                    println "Sample budget request created"
                } else {
                    println "AuditLogService not available"
                }
            }
        }
    }

    def destroy = {
        // Optional cleanup code
    }
}
