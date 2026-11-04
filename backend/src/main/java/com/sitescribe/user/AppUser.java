package com.sitescribe.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sitescribe.company.Company;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private String preferredLanguage = "en";

    @Column(nullable = true)
    private String passwordHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AppUser() {}

    public AppUser(Company company, String name, String email, UserRole role, String preferredLanguage) {
        this.company = company;
        this.name = name;
        this.email = email;
        this.role = role;
        this.preferredLanguage = preferredLanguage == null ? "en" : preferredLanguage;
    }

    public AppUser(Company company, String name, String email, UserRole role,
                   String preferredLanguage, String passwordHash) {
        this(company, name, email, role, preferredLanguage);
        this.passwordHash = passwordHash;
    }

    public UUID getId() { return id; }
    public Company getCompany() { return company; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public String getPreferredLanguage() { return preferredLanguage; }
    @JsonIgnore
    public String getPasswordHash() { return passwordHash; }
}
