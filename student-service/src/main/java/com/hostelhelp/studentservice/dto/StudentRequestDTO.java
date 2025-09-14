package com.hostelhelp.studentservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRequestDTO {

    @NotNull
    private String name;

    @NotNull
    @Email
    private String email;

    @NotNull
    private String password;

    @NotNull
    private Integer graduationYear;

    @NotNull
    private String uid;

    @NotNull
    private String address;

    @NotNull
    private String dateOfBirth;

    @NotNull
    private String gender;

    @NotNull
    private String phone;

    private String hostel;
    private Integer roomNumber;
}
