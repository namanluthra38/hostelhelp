package com.hostelhelp.hostelservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HostelResponseDTO(
        UUID id,
        String name,
        boolean hasAC,
        int numberOfRooms,
        int numberOfSeatsPerRoom,
        double chargesPerSemester,
        boolean isBoysHostel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
