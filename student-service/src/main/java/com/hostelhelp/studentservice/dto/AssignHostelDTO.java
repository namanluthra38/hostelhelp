package com.hostelhelp.studentservice.dto;

import jakarta.validation.constraints.NotNull;

public record AssignHostelDTO(
    @NotNull String hostelId,
    @NotNull Integer roomNumber
) {}

