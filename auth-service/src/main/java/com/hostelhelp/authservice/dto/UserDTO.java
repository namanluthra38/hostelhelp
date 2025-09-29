package com.hostelhelp.authservice.dto;


import com.hostelhelp.authservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;
public record UserDTO(
        String email,
        String password,
        String role
) {}