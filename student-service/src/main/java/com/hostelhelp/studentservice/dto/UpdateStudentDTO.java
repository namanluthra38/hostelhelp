package com.hostelhelp.studentservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateStudentDTO (
    LocalDate dateOfBirth,
    String phone,
    String address
)
{}
