package com.hostelhelp.requestservice.dto;

import com.hostelhelp.requestservice.model.Request.RequestType;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record CreateRequestDTO(
        @NotNull String studentId,
        @NotNull RequestType type,
        @NotNull Map<String, Object> details
) {}
