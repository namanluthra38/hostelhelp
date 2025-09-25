package com.hostelhelp.authservice.dto;

import com.hostelhelp.authservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private final String token;
    private User.Role role;
    private String email;
}
