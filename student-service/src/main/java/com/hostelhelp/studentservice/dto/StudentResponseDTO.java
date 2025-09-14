package com.hostelhelp.studentservice.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponseDTO {

    private String id;

    private String name;

    private String email;

    private Integer graduationYear;

    private String UID;

    private String address;

    private String dateOfBirth;

    private String gender;

    private String phone;

    private String hostel;
    private Integer roomNumber;
}
