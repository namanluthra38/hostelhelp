package com.hostelhelp.requestservice.controller;

import com.hostelhelp.requestservice.dto.CreateRequestDTO;
import com.hostelhelp.requestservice.dto.RequestResponseDTO;
import com.hostelhelp.requestservice.model.Request;
import com.hostelhelp.requestservice.service.RequestService;
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
    public ResponseEntity<RequestResponseDTO> createRequest(@RequestBody CreateRequestDTO dto) {
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

    // Get request by id
    @GetMapping("/{id}")
    public ResponseEntity<RequestResponseDTO> getRequestById(@PathVariable String id) {
        return service.getRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update request status
    @PatchMapping("/{id}/status")
    public ResponseEntity<RequestResponseDTO> updateStatus(
            @PathVariable String id,
            @RequestParam Request.Status status,
            @RequestParam String reviewedBy
    ) {
        return service.updateRequestStatus(id, status, reviewedBy)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete request
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable String id) {
        service.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }
}
