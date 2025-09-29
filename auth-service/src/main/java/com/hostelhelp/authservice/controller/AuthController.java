package com.hostelhelp.authservice.controller;

import com.hostelhelp.authservice.dto.LoginRequestDTO;
import com.hostelhelp.authservice.dto.LoginResponseDTO;
import com.hostelhelp.authservice.dto.UserDTO;
import com.hostelhelp.authservice.model.User;
import com.hostelhelp.authservice.service.AuthService;
import com.hostelhelp.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO loginRequestDTO) {
        Optional<LoginResponseDTO> responseOptional = authService.authenticate(loginRequestDTO);
        if(responseOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LoginResponseDTO responseDTO = responseOptional.get();
        return ResponseEntity.ok(responseDTO);

    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken(
            @RequestHeader("Authorization") String authHeader) {


        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return authService.validateToken(authHeader.substring(7))
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody UserDTO userDTO) {
        User user = User.builder()
                .email(userDTO.email())
                .password(passwordEncoder.encode(userDTO.password()))
                .role(User.Role.valueOf(userDTO.role()))
                .build();

        userService.save(user);
        return ResponseEntity.ok().build();
    }

}
