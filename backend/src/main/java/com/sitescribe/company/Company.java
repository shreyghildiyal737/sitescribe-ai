package com.sitescribe.company;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String preferredLanguage = "en";

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Company() {}

    public Company(String name, String preferredLanguage) {
        this.name = name;
        this.preferredLanguage = preferredLanguage == null ? "en" : preferredLanguage;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public Instant getCreatedAt() { return createdAt; }
}
