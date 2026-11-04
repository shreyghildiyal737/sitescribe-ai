package com.sitescribe.project;

import com.sitescribe.user.AppUser;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_assignments")
public class ProjectAssignment {

    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false)
    private String roleOnProject;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant assignedAt = Instant.now();

    protected ProjectAssignment() {}

    public ProjectAssignment(Project project, AppUser user, String roleOnProject) {
        this.project = project;
        this.user = user;
        this.roleOnProject = roleOnProject;
    }

    public UUID getId() { return id; }
    public Project getProject() { return project; }
    public AppUser getUser() { return user; }
    public String getRoleOnProject() { return roleOnProject; }
    public boolean isActive() { return active; }
}
