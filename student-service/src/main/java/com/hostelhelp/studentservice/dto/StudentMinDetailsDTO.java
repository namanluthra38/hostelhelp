package com.hostelhelp.studentservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record StudentMinDetailsDTO(
        String name,
        String email,
        Integer graduationYear,
        String uid,
        String phone
) {}
