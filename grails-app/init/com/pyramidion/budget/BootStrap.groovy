package com.pyramidion.budget

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Environment
import groovy.json.JsonOutput

@Transactional
class BootStrap {

    AuditLogService auditLogService
    BudgetRequestService budgetRequestService

    def init = { servletContext ->

        // Only seed by default in DEV; skip in TEST to avoid clashes with specs
        // Allow override via -Dbudget.seed=true for local debugging
        boolean shouldSeed =
                (Environment.current == Environment.DEVELOPMENT) ||
                        ("true".equalsIgnoreCase(System.getProperty("budget.seed")))

        if (!shouldSeed) {
            println "BootStrap: seeding skipped for environment ${Environment.current} (set -Dbudget.seed=true to force)"
            return
        }

        seedUsers()
        seedDepartments()
        seedSampleBudgetRequest()
    }

    private void seedUsers() {
        // Idempotent user creation (upsert pattern)
        User admin = User.findByUsername('admin') ?: new User(
                username: 'admin', displayName: 'Admin User', role: User.Role.ADMIN
        ).save(failOnError: true)

        User mgr = User.findByUsername('manager') ?: new User(
                username: 'manager', displayName: 'Product Manager', role: User.Role.MANAGER
        ).save(failOnError: true)

        User barath = User.findByUsername('barath') ?: new User(
                username: 'barath', displayName: 'Barath Elumalai', role: User.Role.USER
        ).save(failOnError: true)

        println "Default users ensured: ${User.count()}"
    }

    private void seedDepartments() {
        // Idempotent department creation; avoids unique name conflicts
        Department it = Department.findByName('IT') ?: new Department(
                name: 'IT',
                currentBudget: 50000G,
                yearlyAllocation: 100000G,
                location: 'Head Office',
                code: 'IT01'
        ).save(failOnError: true)

        Department hr = Department.findByName('HR') ?: new Department(
                name: 'HR',
                currentBudget: 30000G,
                yearlyAllocation: 60000G,
                location: 'Head Office',
                code: 'HR01'
        ).save(failOnError: true)

        println "Default departments ensured: ${Department.count()}"
    }

    private void seedSampleBudgetRequest() {
        User user = User.findByUsername('barath')
        Department itDept = Department.findByName('IT')

        if (!user || !itDept) {
            println "Skipping sample BudgetRequest: prerequisites not present"
            return
        }

        // Only create sample if not already present (prevent duplicates on reruns)
        BudgetRequest existing = BudgetRequest.findByPurposeAndDepartment('Purchase laptops', itDept)
        if (existing) {
            println "Sample budget request already exists (id=${existing.id})"
            return
        }

        BudgetRequest request = new BudgetRequest(
                requestedAmount: 5000G,
                purpose: 'Purchase laptops',
                requestedBy: user,
                department: itDept,
                notes: 'Urgent requirement'
                // dateCreated/lastUpdated are managed by GORM; no manual assignment needed
        ).save(failOnError: true)

        // Optional: audit the creation if service is available
        if (auditLogService) {
            def newJson = JsonOutput.toJson(BudgetUtils.simplifiedRequestMap(request))
            auditLogService.logAction(
                    'CREATED',
                    request.id,
                    'BudgetRequest',
                    null,
                    newJson,
                    user.username
            )
            println "Sample budget request created (id=${request.id})"
        } else {
            println "AuditLogService not available; sample request created without audit"
        }
    }

    def destroy = {
        // Optional cleanup
    }
}
