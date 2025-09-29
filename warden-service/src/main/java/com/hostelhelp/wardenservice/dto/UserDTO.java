package com.hostelhelp.wardenservice.dto;



public record UserDTO(
        String email,
        String password,
        String role
) {}