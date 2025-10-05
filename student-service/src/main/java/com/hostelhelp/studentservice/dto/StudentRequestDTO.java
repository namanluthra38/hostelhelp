package com.hostelhelp.studentservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StudentRequestDTO(
        @NotNull String name,

        @NotNull @Email String email,

        @NotNull @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotNull Integer graduationYear,

        @NotNull String uid,

        @NotNull String address,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate dateOfBirth,

        @NotNull String gender,

        @NotNull String phone


) {}
