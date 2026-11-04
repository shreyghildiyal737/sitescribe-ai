package com.sitescribe.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.project.id = :projectId AND r.status NOT IN ('APPROVED', 'EXPORTED')")
    long countPendingByProjectId(UUID projectId);
}
