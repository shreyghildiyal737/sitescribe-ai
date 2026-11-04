package com.sitescribe.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, UUID> {

    List<ProjectAssignment> findByUserIdAndActiveTrue(UUID userId);

    List<ProjectAssignment> findByProjectIdAndActiveTrue(UUID projectId);

    @Query("SELECT COUNT(a) FROM ProjectAssignment a WHERE a.project.id = :projectId AND a.active = true")
    long countActiveByProjectId(UUID projectId);
}
