package com.hostelhelp.studentservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record StudentResponseDTO(
        UUID id,
        String name,
        String email,
        Integer graduationYear,
        String uid,
        String address,
        LocalDate dateOfBirth,
        String gender,
        String phone,
        String hostelId,
        String roomId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
