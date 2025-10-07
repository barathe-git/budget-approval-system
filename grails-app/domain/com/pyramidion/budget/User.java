package com.pyramidion.budget;

import grails.gorm.annotation.Entity;
import javax.persistence.*;


@Entity
public class User {

    public enum Role { USER, MANAGER, ADMIN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    // Constructor
    public User() {}

    public User(String username, String displayName, Role role) {
        this.username = username;
        this.displayName = displayName;
        this.role = role;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}