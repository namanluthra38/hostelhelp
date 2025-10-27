package com.hostelhelp.studentservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HostelResponseDTO(
        UUID id,
        String name,
        boolean hasAC,
        int numberOfRooms,
        double chargesPerSemester,
        boolean isBoysHostel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
