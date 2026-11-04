package com.sitescribe.project;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteUserRequest(
    @NotBlank String name,
    @Email String email,
    @NotBlank String role,
    @NotBlank String roleOnProject,
    String preferredLanguage
) {}
