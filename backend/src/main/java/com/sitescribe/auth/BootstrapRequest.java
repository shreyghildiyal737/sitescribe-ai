package com.sitescribe.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BootstrapRequest(
    @NotBlank String companyName,
    @NotBlank String name,
    @NotBlank @Email String email,
    String preferredLanguage,
    @NotBlank @Size(min = 8, max = 72, message = "must be 8 to 72 characters") String password
) {}
