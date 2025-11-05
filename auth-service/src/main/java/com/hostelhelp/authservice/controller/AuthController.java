package com.hostelhelp.authservice.controller;

import com.hostelhelp.authservice.dto.LoginRequestDTO;
import com.hostelhelp.authservice.dto.LoginResponseDTO;
import com.hostelhelp.authservice.dto.UserDTO;
import com.hostelhelp.authservice.model.User;
import com.hostelhelp.authservice.service.AuthService;
import com.hostelhelp.authservice.service.UserService;
import com.hostelhelp.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final Logger log = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO loginRequestDTO) {
        log.info("login request: {}", loginRequestDTO);
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

    // New endpoint: return the role encoded in the Bearer token.
    @GetMapping("/role")
    public ResponseEntity<String> getRoleFromToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);
        try {
            if (!authService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String role = jwtUtil.getRoleFromToken(token);
            if (role == null) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(role);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/id")
    public ResponseEntity<String> getIdFromToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);
        try {
            if (!authService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String id = jwtUtil.getRoleFromToken(token);
            if (id == null) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(id);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody UserDTO userDTO) {
        User user = User.builder()
                .email(userDTO.email())
                .password(userDTO.password())
                .role(User.Role.valueOf(userDTO.role()))
                .build();

        userService.save(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/user/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        if (!userService.existsByEmail(email)) {
            return ResponseEntity.notFound().build();
        }
        
        userService.deleteByEmail(email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/email/{email}")
    public ResponseEntity<UUID> getUserIdByEmail(@PathVariable String email) {
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userOptional.get().getId());
    }

}
