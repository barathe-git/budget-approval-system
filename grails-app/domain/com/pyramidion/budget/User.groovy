package com.pyramidion.budget

import grails.gorm.annotation.Entity
import java.time.LocalDateTime

@Entity
class User {

    enum Role { USER, MANAGER, ADMIN }

    String username
    String displayName
    Role role = Role.USER

    LocalDateTime dateCreated
    LocalDateTime lastUpdated

    static constraints = {
        username nullable: false, unique: true, maxSize: 100
        displayName nullable: false, maxSize: 150
        role nullable: false
    }

    static mapping = {
        username column: 'username', type: 'string'
    }

    String toString() {
        "User(id=${id}, username=${username}, role=${role})"
    }
}
