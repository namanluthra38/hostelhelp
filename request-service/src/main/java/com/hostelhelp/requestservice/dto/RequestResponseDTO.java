package com.hostelhelp.requestservice.dto;

import com.hostelhelp.requestservice.model.Request.RequestType;
import com.hostelhelp.requestservice.model.Request.Status;
import java.time.LocalDateTime;
import java.util.Map;

public record RequestResponseDTO(
        String id,
        String studentId,
        String hostelId,
        RequestType type,
        Map<String, Object> details,
        Status status,
        String reviewedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

