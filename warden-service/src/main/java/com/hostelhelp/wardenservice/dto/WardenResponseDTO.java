package com.hostelhelp.wardenservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WardenResponseDTO(
        UUID id,
        String name,
        String email,
        String gender,
        String phone,
        String hostel,
        Integer roomNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
