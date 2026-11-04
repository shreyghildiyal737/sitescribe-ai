package com.sitescribe.project;

import java.util.UUID;

public record ProjectMember(
    UUID userId,
    String name,
    String email,
    String roleOnProject
) {}
