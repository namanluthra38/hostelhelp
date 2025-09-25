package com.hostelhelp.authservice.service;

import com.hostelhelp.authservice.dto.LoginRequestDTO;
import com.hostelhelp.authservice.dto.LoginResponseDTO;
import com.hostelhelp.authservice.model.User;
import com.hostelhelp.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;


    public Optional<LoginResponseDTO> authenticate(LoginRequestDTO loginRequestDTO) {
        Optional<User> userOptional = userService.findByEmail(loginRequestDTO.getEmail());
        if(userOptional.isEmpty()){
            return Optional.empty();
        }
        User user = userOptional.get();
        if(passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())){

            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            LoginResponseDTO responseDTO = new LoginResponseDTO(token, user.getRole(), user.getEmail());
            return Optional.of(responseDTO);
        }
        else return Optional.empty();
    }

    public boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (JwtException e){
            return false;
        }
    }
}
