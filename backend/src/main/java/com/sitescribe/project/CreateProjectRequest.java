package com.sitescribe.project;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateProjectRequest(
    @NotBlank String name,
    String address,
    BigDecimal latitude,
    BigDecimal longitude
) {}
