package com.hostelhelp.requestservice.controller;

import com.hostelhelp.requestservice.dto.CreateRequestDTO;
import com.hostelhelp.requestservice.dto.RequestResponseDTO;
import com.hostelhelp.requestservice.exception.NoVacantRoomException;
import com.hostelhelp.requestservice.exception.RemoteServiceException;
import com.hostelhelp.requestservice.exception.UnauthorizedActionException;
import com.hostelhelp.requestservice.model.Request;
import com.hostelhelp.requestservice.service.RequestService;
import com.hostelhelp.requestservice.exception.StudentNotFoundRemoteException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService service;
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RequestController.class);

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
    public ResponseEntity<List<RequestResponseDTO>> getRequestsByHostel(@PathVariable String hostelId, @RequestHeader("Authorization") String authHeader) {
        System.out.println("HostelId: " + hostelId);
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
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            log.warn("Missing or malformed Authorization header for request {}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        log.info("Request [{}] status update attempt -> {} by {}", id, status, reviewedBy);

        try {
            Optional<RequestResponseDTO> opt = service.updateRequestStatus(id, status, reviewedBy, token);
            return opt.map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        log.info("Request {} not found", id);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });
        } catch (UnauthorizedActionException ex) {
            log.warn("Unauthorized attempt to update request {}: {}", id, ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (StudentNotFoundRemoteException ex) {
            log.error("Remote student not found while processing request {}: {}", id, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (NoVacantRoomException ex) {
            log.error("No vacant room when processing request {}: {}", id, ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("Bad request while processing request {}: {}", id, ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (RemoteServiceException ex) {
            log.error("Remote service error while processing request {}: {}", id, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error while updating request {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error");
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
            return ResponseEntity.status(204).body("Not Found");
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
            return ResponseEntity.status(204).body("Not Found");
        }
    }
}
