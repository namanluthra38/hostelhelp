package com.hostelhelp.wardenservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WardenRequestDTO {
    @NotNull(message = "Name cannot be null")
    private String name;

    @NotNull(message = "Email cannot be null")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Password cannot be null")
    private String password;

    @NotNull(message = "Gender cannot be null")
    private String gender;

    @NotNull(message = "Phone number cannot be null")
    private String phone;

    private String hostel;
    private Integer roomNumber;
}
