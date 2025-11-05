package com.hostelhelp.requestservice.service;

import com.hostelhelp.requestservice.dto.CreateRequestDTO;
import com.hostelhelp.requestservice.dto.RequestResponseDTO;
import com.hostelhelp.requestservice.mapper.RequestMapper;
import com.hostelhelp.requestservice.model.Request;
import com.hostelhelp.requestservice.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService {


    private final RequestRepository repository;
    private final RestTemplate restTemplate;

    // Create a new request
    public RequestResponseDTO createRequest(CreateRequestDTO dto) {
        Request request = RequestMapper.toEntity(dto);
        repository.save(request);
        return RequestMapper.toResponse(request);
    }


    // Allow new HOSTEL_JOIN if any existing pending join is for a different hostel
    public RequestResponseDTO createJoinRequest(CreateRequestDTO dto) {

        String incomingHostelId = dto.hostelId();

        log.info("Creating join request for student={} hostelId={}", dto.studentId(), incomingHostelId);

        // If no hostel specified in incoming request, block if any pending HOSTEL_JOIN exists
        if (incomingHostelId == null) {
            boolean conflict = repository.findByStudentId(dto.studentId())
                    .stream()
                    .anyMatch(r -> r.getType() == Request.RequestType.HOSTEL_JOIN && r.getStatus() == Request.Status.PENDING);

            if (conflict) {
                log.warn("A pending hostel join request already exists for student={} and hostel={}", dto.studentId(), incomingHostelId);
                throw new IllegalStateException("A pending hostel join request already exists for this student and hostel.");
            }
        } else {
            // Reuse the helper to check whether a pending HOSTEL_JOIN request exists for given student and hostel
            boolean exists = existsPendingJoinRequestForStudentAndHostel(dto.studentId(), incomingHostelId);
            if (exists) {
                log.warn("A pending hostel join request already exists for student={} and hostel={}", dto.studentId(), incomingHostelId);
                throw new IllegalStateException("A pending hostel join request already exists for this student and hostel.");
            }
        }

        Request request = RequestMapper.toEntity(dto);
        request.setStatus(Request.Status.PENDING);
        repository.save(request);
        return RequestMapper.toResponse(request);
    }

    public RequestResponseDTO createLeaveRequest(CreateRequestDTO dto) {

        Object roomIdObj = (dto.details() != null) ? dto.details().get("roomId") : null;
        String incomingRoomId = (roomIdObj == null) ? null : String.valueOf(roomIdObj);

        log.info("Creating leave request for student={} hostelId={}", dto.studentId(), incomingRoomId);

        if (incomingRoomId == null) {
            throw new IllegalStateException("No hostel specified in request. Cannot create a leave request.");
        } else {
            boolean exists = existsPendingLeaveRequest(dto.studentId());
            if (exists) {
                log.warn("A pending hostel leave request already exists for student={}", dto.studentId());
                throw new IllegalStateException("A pending hostel leave request already exists for this student");
            }
        }

        Request request = RequestMapper.toEntity(dto);
        request.setStatus(Request.Status.PENDING);
        repository.save(request);
        return RequestMapper.toResponse(request);
    }

    public boolean existsPendingLeaveRequest(String studentId) {
        if (studentId == null) {
            return false;
        }
        log.info("Checking existence of PENDING HOSTEL_JOIN request for student={}", studentId);
        return repository.findByStudentId(studentId)
                .stream()
                .anyMatch(r -> {
                    if (r.getType() != Request.RequestType.HOSTEL_LEAVE) return false;
                    else return r.getStatus() == Request.Status.PENDING;
                });
    }

    public List<RequestResponseDTO> getAllRequests() {
        return repository.findAll()
                .stream()
                .map(RequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Get requests by student
    public List<RequestResponseDTO> getRequestsByStudent(String studentId) {
        return repository.findByStudentId(studentId)
                .stream()
                .map(RequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Get a single request
    public Optional<RequestResponseDTO> getRequestById(String id) {
        return repository.findById(id).map(RequestMapper::toResponse);
    }

    // Update request status & reviewedBy
    @SuppressWarnings("unused")
    public Optional<RequestResponseDTO> updateRequestStatus(String id, Request.Status status, String reviewedBy, String token) throws Exception {

        Optional<Request> req = repository.findById(id);
        // If request not found, return empty Optional
        if (req.isEmpty()) return Optional.empty();

        if (token == null || token.isBlank()) {
            throw new Exception("unauthorized");
        }

        String callerRole;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            // Note: calling auth-service to get role via api-gateway
            callerRole = restTemplate.exchange("http://api-gateway:4004/auth/role", HttpMethod.GET, entity, String.class).getBody();
        } catch (Exception e) {
            log.warn("Failed to fetch role from auth service: {}", e.getMessage());
            throw new Exception("unauthorized");
        }

        if (callerRole == null) throw new Exception("unauthorized");
        String roleUpper = callerRole.trim().toUpperCase();

        if ("STUDENT".equals(roleUpper)) {
            throw new Exception("unauthorized");
        }

        if ("WARDEN".equals(roleUpper)) {
            // fetch warden's hostelId and compare with request's hostelId
            String wardenHostelId;
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                wardenHostelId = restTemplate.exchange("http://api-gateway:4004/wardens/me/hostelId", HttpMethod.GET, entity, String.class).getBody();
            } catch (Exception e) {
                log.warn("Failed to fetch warden hostelId: {}", e.getMessage());
                throw new Exception("unauthorized");
            }

            Request requestObj = req.get();
            Object existingHostelObj = requestObj.getHostelId();
            String requestHostelId = (existingHostelObj == null) ? null : String.valueOf(existingHostelObj);

            if (requestHostelId == null) {
                // If the request doesn't contain a hostelId we can't authorize the warden
                throw new Exception("unauthorized");
            }

            if (!requestHostelId.equals(wardenHostelId)) {
                throw new Exception("unauthorized");
            }
        }

         return req.map(request -> {
             request.setStatus(status);
             request.setReviewedBy(reviewedBy);
             repository.save(request);
             try {
                 if (request.getType() == Request.RequestType.HOSTEL_JOIN && request.getStatus() == Request.Status.APPROVED) {
                     log.info("Assigning hostel to student: {} for request {}", request.getStudentId(), id);
                     assignHostelIfApproved(request, token);
                     log.info("Hostel assigned successfully to student: {} for request {}", request.getStudentId(), id);
                 }
                 if (request.getType() == Request.RequestType.HOSTEL_LEAVE && request.getStatus() == Request.Status.APPROVED) {
                     log.info("Trying to leave hostel for student: {} for request {}", request.getStudentId(), id);
                     leaveHostelIfApproved(request, token);
                     log.info("Hostel left successfully for student: {} for request {}", request.getStudentId(), id);
                 }
             } catch (Exception e) {
                 log.error(e.getMessage());
                 request.setStatus(Request.Status.PENDING);
                 request.setReviewedBy(null);
                 throw e;
             }
             return RequestMapper.toResponse(request);
         });
     }

    @SuppressWarnings("unused")
    private void leaveHostelIfApproved(Request request, String token) {


        String studentId = request.getStudentId();
        if (studentId == null) {
            log.warn("Cannot process leave: missing studentId for request {}", request.getId());
            return;
        }


        // Prefer roomId provided in the request details (if available)
        String roomId = null;
        if (request.getDetails() != null && request.getDetails().get("roomId") != null) {
            roomId = String.valueOf(request.getDetails().get("roomId"));
        } else {
            try {
                log.debug("Attempting to fetch student to determine roomId for student {}", studentId);
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ParameterizedTypeReference<Map<String, Object>> ptr = new ParameterizedTypeReference<>() {};
                Map<String, Object> studentObj = restTemplate.exchange("http://api-gateway:4004/students/" + studentId, HttpMethod.GET, entity, ptr).getBody();
                if (studentObj != null && studentObj.get("roomId") != null) {
                    roomId = String.valueOf(studentObj.get("roomId"));
                }
            } catch (Exception e) {
                // Simplified handling: log and continue. If we couldn't fetch student, we'll still try to call leave.
                log.warn("Could not fetch student {} to determine roomId: {}. Proceeding to process leave anyway.", studentId, e.getMessage());
            }
        }

        // If we have a roomId, attempt to remove the student from that room. If that call fails, log and continue.
        if (roomId != null) {
            try {
                log.info("Removing student {} from room {}", studentId, roomId);
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                String removeUrl = "http://api-gateway:4004/hostels/rooms/remove-student?studentId=" + studentId + "&roomId=" + roomId;
                restTemplate.exchange(removeUrl, HttpMethod.POST, entity, Object.class);
                log.info("Removed student {} from room {} successfully", studentId, roomId);
            } catch (Exception e) {
                // Don't fail the entire leave flow if removing from room fails; just log.
                log.warn("Failed to remove student {} from room {}: {}", studentId, roomId, e.getMessage());
            }
        } else {
            log.debug("No roomId available for student {}. Skipping room removal.", studentId);
        }

        // Finally, call student-service leave endpoint to update student record. Let errors surface as runtime exceptions.
        try {
            log.info("Calling student-service leave endpoint for student {}", studentId);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange("http://api-gateway:4004/students/" + studentId + "/leave", HttpMethod.POST, entity, Object.class);
            log.info("Student {} leave processed in student-service", studentId);
        } catch (Exception e) {
            log.error("Failed to process leave for student {}: {}", studentId, e.getMessage());
            throw new RuntimeException("Failed to process leave for student " + studentId + ": " + e.getMessage(), e);
        }
    }

    // Delete a request
    public void deleteRequest(String id) {
        repository.deleteById(id);
    }


    private void assignHostelIfApproved(Request request, String token) {
        String studentId = request.getStudentId();
        String hostelId = request.getHostelId();

        if (studentId == null || hostelId == null) {
            log.warn("Cannot assign hostel: missing studentId or hostelId for request {}", request.getId());
            return;
        }

        log.info("assigning hostel with id: {} to student {}", hostelId, studentId);
        String roomAssignUrl = "http://api-gateway:4004/hostels/rooms/allocate?hostelId=" + hostelId + "&studentId=" + studentId;
        try {
            log.info("Calling room allocation endpoint for hostel {} student {}", hostelId, studentId);
            HttpHeaders headers = new HttpHeaders();
            if (token != null && !token.isBlank()) headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(roomAssignUrl, HttpMethod.POST, entity, Void.class);
            log.info("Room assigned successfully in hostel {} to student {}", hostelId, studentId);
            for(Request r :
                    repository.findByStudentId(studentId).stream().filter(
                            req ->
                                    req.getType() == Request.RequestType.HOSTEL_JOIN
                                    && req.getStatus() == Request.Status.PENDING
                                    ).toList()){
                r.setReviewedBy("AUTO");
                r.setStatus(Request.Status.REJECTED);
                repository.save(r);
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                log.error("Room assignment failed: student or hostel not found for student {} in hostel {}", studentId, hostelId);
            } else if (e.getStatusCode().value() == 403) {
                log.error("Forbidden when assigning room for student {}: {}", studentId, e.getMessage());
                throw new RuntimeException("Forbidden call to room-service. Check permissions.");
            } else {
                log.error("Unexpected error assigning room for student {}: {}", studentId, e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error assigning room for student {}: {}", studentId, e.getMessage());
            throw new RuntimeException("Failed to assign room: " + e.getMessage());
        }
    }

    // New helper: check whether any pending HOSTEL_JOIN request exists for given studentId and hostelId
    public boolean existsPendingJoinRequestForStudentAndHostel(String studentId, String hostelId) {
        if (studentId == null || hostelId == null) {
            return false;
        }
        log.info("Checking existence of PENDING HOSTEL_JOIN request for student={} hostelId={}", studentId, hostelId);
        return repository.findByStudentId(studentId)
                .stream()
                .anyMatch(r -> {
                    if (r.getType() != Request.RequestType.HOSTEL_JOIN) return false;
                    if (r.getStatus() != Request.Status.PENDING) return false;
                    String existingHostelId = r.getHostelId();
                    return hostelId.equals(existingHostelId);
                });
    }


    public List<RequestResponseDTO> getRequestsByHostel(String hostelId) {
        // If caller didn't provide a hostelId, return an empty list rather than matching nulls.
        if (hostelId == null) {
            return List.of();
        }

        return repository.findAll()
                .stream()
                .filter(request -> java.util.Objects.equals(hostelId, request.getHostelId()))
                .map(RequestMapper::toResponse)
                .collect(Collectors.toList());

    }
}
