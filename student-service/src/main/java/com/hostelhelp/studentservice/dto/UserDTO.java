package com.hostelhelp.studentservice.dto;



public record UserDTO(
        String email,
        String password,
        String role
) {}