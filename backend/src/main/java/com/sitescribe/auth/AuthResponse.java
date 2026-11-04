package com.sitescribe.auth;

import java.util.UUID;

public record AuthResponse(
    UUID userId,
    UUID companyId,
    String name,
    String email,
    String role,
    String token
) {}
