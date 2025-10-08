package com.pyramidion.budget

class UrlMappings {
    static mappings = {
        // Standard RESTful routes (plural)
        "/api/budget-requests"(resources: 'budgetRequest')

        // Budget request APIs
        post "/api/budget-request"(controller: 'budgetRequest', action: 'create')
        get  "/api/budget-request"(controller: 'budgetRequest', action: 'index')
        get  "/api/budget-request/$id"(controller: 'budgetRequest', action: 'show')
        put  "/api/budget-request/$id/approve"(controller: 'budgetRequest', action: 'approve')
        put  "/api/budget-request/$id/reject"(controller: 'budgetRequest', action: 'reject')
        get  "/api/budget-request/pending"(controller: 'budgetRequest', action: 'pending')

        // Audit log APIs
        "/api/audit-logs"(controller: "auditLog", action: "index")

        "/$controller/$action?/$id?(.$format)?"()
        "/"(view: "/index")
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}

