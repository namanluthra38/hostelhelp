package com.hostelhelp.studentservice.controller;

import com.hostelhelp.studentservice.dto.HostelResponseDTO;
import com.hostelhelp.studentservice.dto.RoomResponseDTO;
import com.hostelhelp.studentservice.dto.StudentCompositeDTO;
import com.hostelhelp.studentservice.dto.StudentResponseDTO;
import com.hostelhelp.studentservice.exception.StudentNotFoundException;
import com.hostelhelp.studentservice.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentCompositeController {

    private final StudentService studentService;
    private final RestTemplateBuilder restTemplateBuilder;
    private final ObjectMapper objectMapper; // injected

    @Value("${services.hostel.base-url:http://api-gateway:4004}")
    private String hostelServiceBaseUrl;

    /**
     * GET /students/me/full
     * Returns StudentCompositeDTO { student, room, hostel }
     */
    @GetMapping("/me/full")
    public ResponseEntity<StudentCompositeDTO> getStudentWithRoomAndHostel(HttpServletRequest request) {
        try {
            // 1) get email from SecurityContext (preferred)
            String email = null;
            try {
                var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) {
                    email = auth.getName();
                }
            } catch (Exception e) {
                log.debug("Unable to read SecurityContextHolder authentication", e);
            }

            // 1b) fallback: extract from Authorization header if SecurityContext not populated
            if (email == null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
                try {
                    String extracted = extractEmailFromAuthorizationHeader(authHeader);
                    if (extracted != null) {
                        email = extracted;
                    }
                } catch (Exception e) {
                    log.warn("Failed to extract email from Authorization header: {}", e.getMessage());
                }

                if (email == null) {
                    log.warn("SecurityContextHolder has no authentication; Authorization header present but cannot derive email.");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            }

            // 2) fetch student locally
            StudentResponseDTO student = studentService.getStudentByEmail(email);

            // Prepare RestTemplate and headers (forward Authorization)
            RestTemplate restTemplate = restTemplateBuilder
                    .requestFactory(() -> {
                        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                        factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
                        factory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());
                        return factory;
                    })
                    .build();

            HttpHeaders headers = new HttpHeaders();
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null) {
                headers.set(HttpHeaders.AUTHORIZATION, authHeader);
            }
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            RoomResponseDTO roomDto = null;
            HostelResponseDTO hostelDto = null;

            // 3) fetch room if student has roomId
            try {
                if (student.roomId() != null) {
                    String roomUrl = String.format("%s/hostels/rooms/%s", hostelServiceBaseUrl, student.roomId());
                    ResponseEntity<RoomResponseDTO> roomResp = restTemplate.exchange(roomUrl, HttpMethod.GET, entity, RoomResponseDTO.class);
                    roomDto = roomResp.getBody();
                }
            } catch (HttpClientErrorException.NotFound nfe) {
                log.debug("Room not found for id {}: {}", student.roomId(), nfe.getMessage());
                roomDto = null;
            } catch (Exception ex) {
                log.error("Error fetching room for student {}: {}", student.id(), ex.getMessage());
                roomDto = null;
            }

            // 4) fetch hostel: prefer student.hostelId, else try nested hostelId from room
            try {
                if (student.hostelId() != null) {
                    String hostelUrl = String.format("%s/hostels/%s", hostelServiceBaseUrl, student.hostelId());
                    ResponseEntity<HostelResponseDTO> hostelResp = restTemplate.exchange(hostelUrl, HttpMethod.GET, entity, HostelResponseDTO.class);
                    hostelDto = hostelResp.getBody();
                } else if (roomDto != null && roomDto.hostelId() != null) {
                    String hostelUrl = String.format("%s/hostels/%s", hostelServiceBaseUrl, roomDto.hostelId());
                    ResponseEntity<HostelResponseDTO> hostelResp = restTemplate.exchange(hostelUrl, HttpMethod.GET, entity, HostelResponseDTO.class);
                    hostelDto = hostelResp.getBody();
                }
            } catch (HttpClientErrorException.NotFound nfe) {
                log.debug("Hostel not found: {}", nfe.getMessage());
                hostelDto = null;
            } catch (Exception ex) {
                log.error("Error fetching hostel for student {}: {}", student.id(), ex.getMessage());
                hostelDto = null;
            }

            StudentCompositeDTO composite = new StudentCompositeDTO(student, roomDto, hostelDto);
            return ResponseEntity.ok(composite);

        } catch (StudentNotFoundException snf) {
            log.warn("Student not found for current principal", snf);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Failed to build student composite response", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Helper: extract email/sub from a Bearer JWT without validating signature (best-effort fallback).
     *
     * SECURITY NOTE:
     * - This method decodes the JWT payload without validating the signature.
     * - It's acceptable as a convenience/fallback in local/dev environments, but it MUST NOT be relied on for auth in production.
     * - In production, ensure SecurityContext is populated by your auth filter or validate the token properly here.
     */
    private String extractEmailFromAuthorizationHeader(String authHeader) {
        if (authHeader == null) return null;
        if (!authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) return null;
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String payload = parts[1];
            // Base64url decode
            byte[] decoded = java.util.Base64.getUrlDecoder().decode(payload);
            String json = new String(decoded, StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            // common JWT claim names: "email", "sub", "username"
            Object v = map.get("email");
            if (v == null) v = map.get("sub");
            if (v == null) v = map.get("username");
            if (v != null) return v.toString();
        } catch (Exception e) {
            log.debug("JWT parse failed: {}", e.getMessage());
        }
        return null;
    }
}
