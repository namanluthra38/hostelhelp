package com.hostelhelp.requestservice.controller;

import com.hostelhelp.requestservice.dto.CreateRequestDTO;
import com.hostelhelp.requestservice.dto.RequestResponseDTO;
import com.hostelhelp.requestservice.model.Request;
import com.hostelhelp.requestservice.service.RequestService;
import com.hostelhelp.requestservice.service.StudentNotFoundRemoteException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService service;

    // Create a new request
    @PostMapping
    public ResponseEntity<RequestResponseDTO> createRequest(@Valid @RequestBody CreateRequestDTO dto) {
        return ResponseEntity.ok(service.createRequest(dto));
    }

    // Get all requests
    @GetMapping
    public ResponseEntity<List<RequestResponseDTO>> getAllRequests() {
        return ResponseEntity.ok(service.getAllRequests());
    }

    // Get requests by studentId
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<RequestResponseDTO>> getRequestsByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(service.getRequestsByStudent(studentId));
    }

    @GetMapping("/hostel/{hostelId}")
    public ResponseEntity<List<RequestResponseDTO>> getRequestsByHostel(@PathVariable String hostelId) {
        return ResponseEntity.ok(service.getRequestsByHostel(hostelId));
    }

    // Get request by id
    @GetMapping("/{id}")
    public ResponseEntity<RequestResponseDTO> getRequestById(@PathVariable String id) {
        return service.getRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update request status
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String id,
            @RequestParam Request.Status status,
            @RequestParam String reviewedBy,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.substring(7); // remove "Bearer "
            System.out.println("Token: " + token);
            return service.updateRequestStatus(id, status, reviewedBy, token)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (StudentNotFoundRemoteException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }

    // Delete request
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable String id) {
        service.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }

    // Student creates a hostel join request
    @PostMapping("/join")
    public ResponseEntity<?> createJoinRequest(@Valid @RequestBody CreateRequestDTO dto) {
        if (dto.type() != Request.RequestType.HOSTEL_JOIN) {
            return ResponseEntity.badRequest().body("Request type must be HOSTEL_JOIN");
        }
        try {
            RequestResponseDTO response = service.createJoinRequest(dto);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // Conflict: pending request for same hostel
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @PostMapping("/leave")
    public ResponseEntity<?> createLeaveRequest(@Valid @RequestBody CreateRequestDTO dto) {
        if (dto.type() != Request.RequestType.HOSTEL_LEAVE) {
            return ResponseEntity.badRequest().body("Request type must be HOSTEL_LEAVE");
        }
        try {
            RequestResponseDTO response = service.createLeaveRequest(dto);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // New endpoint: check existence of a request for a given studentId and hostelId
    @GetMapping("/exist")
    public ResponseEntity<String> existsJoinRequest(@RequestParam(required = false) String studentId,
                                                @RequestParam(required = false) String hostelId) {
        if (studentId == null || hostelId == null) {
            return ResponseEntity.badRequest().body("studentId and hostelId query parameters are required");
        }

        boolean exists = service.existsPendingJoinRequestForStudentAndHostel(studentId, hostelId);
        if (exists) {
            return ResponseEntity.ok("Found");
        } else {
            return ResponseEntity.status(404).body("Not Found");
        }
    }

    @GetMapping("/exist-leave")
    public ResponseEntity<String> existsLeaveRequest(@RequestParam(required = false) String studentId) {
        if (studentId == null) {
            return ResponseEntity.badRequest().body("studentId and hostelId query parameters are required");
        }

        boolean exists = service.existsPendingLeaveRequest(studentId);
        if (exists) {
            return ResponseEntity.ok("Found");
        } else {
            return ResponseEntity.status(404).body("Not Found");
        }
    }
}
