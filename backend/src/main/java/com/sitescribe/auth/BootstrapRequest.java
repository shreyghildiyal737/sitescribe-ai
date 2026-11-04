package com.sitescribe.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record BootstrapRequest(
    @NotBlank String companyName,
    @NotBlank String name,
    @Email String email,
    String preferredLanguage,
    @NotBlank String password
) {}
