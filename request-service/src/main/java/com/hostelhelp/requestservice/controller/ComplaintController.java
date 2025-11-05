package com.hostelhelp.requestservice.controller;

import com.hostelhelp.requestservice.dto.CreateComplaintDTO;
import com.hostelhelp.requestservice.model.Complaint;
import com.hostelhelp.requestservice.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;
    private final Logger log = LoggerFactory.getLogger(ComplaintController.class);


    @PostMapping
    public ResponseEntity<?> createComplaint(@Validated @RequestBody CreateComplaintDTO req,
                                             @RequestHeader(name = "Authorization", required = false) String authHeader
    ) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
            }
            String token = authHeader.substring(7);
            Complaint created = complaintService.createComplaint(req, token);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException iae) {
            log.warn("Invalid input for createComplaint: {}", iae.getMessage());
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (Exception e) {
            log.error("Failed to create complaint: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create complaint: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComplaint(@PathVariable String id, @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
            }
            String token = authHeader.substring(7);
            complaintService.deleteComplaint(id, token);
            return ResponseEntity.noContent().build();
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(se.getMessage());
        } catch (Exception e) {
            log.error("Failed to delete complaint {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete complaint: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getComplaint(@PathVariable String id, @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
            }
            String token = authHeader.substring(7);
            Complaint c = complaintService.getComplaint(id, token);
            if (c == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(c);
        } catch (Exception e) {
            log.error("Failed to fetch complaint {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch complaint: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllComplaints(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
            }
            String token = authHeader.substring(7);
            List<Complaint> list = complaintService.getAllComplaints(token);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("Failed to list complaints: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to list complaints: " + e.getMessage());
        }
    }

    @GetMapping("/hostel/{hostelId}")
    public ResponseEntity<?> getHostelComplaints(@PathVariable String hostelId) {
        try {
            List<Complaint> list = complaintService.getHostelComplaints(hostelId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("Failed to list hostel complaints {}: {}", hostelId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to list hostel complaints: " + e.getMessage());
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentComplaints(@PathVariable String studentId) {
        try {
            List<Complaint> list = complaintService.getStudentComplaints(studentId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("Failed to list student complaints {}: {}", studentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to list student complaints: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> patchStatus(@PathVariable String id,
                                         @RequestParam String status,
                                         @RequestHeader(name = "Authorization", required = false) String authHeader) {
        try {
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) token = authHeader.substring(7);
            Complaint updated = complaintService.updateStatus(id, status, token);
            if (updated == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(se.getMessage());
        } catch (Exception e) {
            log.error("Failed to update status for complaint {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update status: " + e.getMessage());
        }
    }

}
