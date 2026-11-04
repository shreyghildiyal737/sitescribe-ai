package com.sitescribe.project;

import com.sitescribe.auth.UserPrincipal;
import com.sitescribe.common.NotFoundException;
import com.sitescribe.company.CompanyRepository;
import com.sitescribe.report.ReportRepository;
import com.sitescribe.user.AppUser;
import com.sitescribe.user.UserRepository;
import com.sitescribe.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projects;
    private final ProjectAssignmentRepository assignments;
    private final CompanyRepository companies;
    private final UserRepository users;
    private final ReportRepository reports;

    public ProjectService(ProjectRepository projects, ProjectAssignmentRepository assignments,
                          CompanyRepository companies, UserRepository users, ReportRepository reports) {
        this.projects = projects;
        this.assignments = assignments;
        this.companies = companies;
        this.users = users;
        this.reports = reports;
    }

    @Transactional
    public Project create(CreateProjectRequest req, UserPrincipal principal) {
        var company = companies.findById(principal.companyId())
            .orElseThrow(() -> new NotFoundException("Company not found"));
        var creator = users.findById(principal.userId())
            .orElseThrow(() -> new NotFoundException("User not found"));

        var project = projects.save(new Project(company, req.name(), req.address(), req.latitude(), req.longitude()));
        assignments.save(new ProjectAssignment(project, creator, creator.getRole().name()));
        return project;
    }

    @Transactional
    public ProjectAssignment invite(UUID projectId, InviteUserRequest req, UserPrincipal principal) {
        var project = requireProjectInCompany(projectId, principal.companyId());
        var role = UserRole.valueOf(req.role().toUpperCase());
        var user = users.findByEmail(req.email())
            .orElseGet(() -> users.save(new AppUser(project.getCompany(), req.name(), req.email(), role, req.preferredLanguage())));
        return assignments.save(new ProjectAssignment(project, user, req.roleOnProject()));
    }

    public List<ProjectSummary> myProjects(UUID userId) {
        return assignments.findByUserIdAndActiveTrue(userId).stream()
            .map(assignment -> {
                var project = assignment.getProject();
                long workerCount = assignments.countActiveByProjectId(project.getId());
                long pendingCount = reports.countPendingByProjectId(project.getId());
                return new ProjectSummary(project.getId(), project.getName(), project.getAddress(),
                    assignment.getRoleOnProject(), workerCount, pendingCount);
            })
            .toList();
    }

    public List<ProjectMember> getMembers(UUID projectId, UserPrincipal principal) {
        requireProjectInCompany(projectId, principal.companyId());
        return assignments.findByProjectIdAndActiveTrue(projectId).stream()
            .map(a -> new ProjectMember(a.getUser().getId(), a.getUser().getName(),
                a.getUser().getEmail(), a.getRoleOnProject()))
            .toList();
    }

    public Project get(UUID id, UserPrincipal principal) {
        return requireProjectInCompany(id, principal.companyId());
    }

    private Project requireProjectInCompany(UUID projectId, UUID companyId) {
        var project = projects.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found"));
        if (!project.getCompany().getId().equals(companyId))
            throw new NotFoundException("Project not found");
        return project;
    }
}
