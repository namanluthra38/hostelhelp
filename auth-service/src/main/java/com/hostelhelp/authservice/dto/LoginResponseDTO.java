package com.hostelhelp.authservice.dto;

import com.hostelhelp.authservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private User.Role role;
    private String email;
}
