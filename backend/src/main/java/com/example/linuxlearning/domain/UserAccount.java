package com.example.linuxlearning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 80)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private LearningPath learningPath;

    protected UserAccount() {
    }

    public UserAccount(String username, String displayName, Role role, LearningPath learningPath) {
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.learningPath = learningPath;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Role getRole() {
        return role;
    }

    public LearningPath getLearningPath() {
        return learningPath;
    }
}
