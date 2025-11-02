package com.hostelhelp.requestservice.dto;

import com.hostelhelp.requestservice.model.Request.RequestType;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record CreateRequestDTO(
        @NotNull String studentId,
        @NotNull String hostelId,
        @NotNull RequestType type,
        Map<String, Object> details
) {}
