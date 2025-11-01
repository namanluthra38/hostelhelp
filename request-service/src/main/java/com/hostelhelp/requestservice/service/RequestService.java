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

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        Object hostelIdObj = (dto.details() != null) ? dto.details().get("hostelId") : null;
        String incomingHostelId = (hostelIdObj == null) ? null : String.valueOf(hostelIdObj);

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
    public Optional<RequestResponseDTO> updateRequestStatus(String id, Request.Status status, String reviewedBy, String token) {
        return repository.findById(id).map(request -> {
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
                Map studentObj = restTemplate.exchange("http://localhost:4000/students/" + studentId, HttpMethod.GET, entity, Map.class).getBody();
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
                String removeUrl = "http://localhost:4001/hostels/rooms/remove-student?studentId=" + studentId + "&roomId=" + roomId;
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
            restTemplate.exchange("http://localhost:4000/students/" + studentId + "/leave", HttpMethod.POST, entity, Object.class);
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
        String hostelId = (String) request.getDetails().get("hostelId");

        if (studentId == null || hostelId == null) {
            log.warn("Cannot assign hostel: missing studentId or hostelId for request {}", request.getId());
            return;
        }

        log.info("assigning hostel with id: {} to student {}", hostelId, studentId);
        String roomAssignUrl = "http://localhost:4001/hostels/rooms/allocate?hostelId=" + hostelId + "&studentId=" + studentId;
        try {
            log.info("Calling room allocation endpoint for hostel {} student {}", hostelId, studentId);
            HttpHeaders headers = new HttpHeaders();
            if (token != null && !token.isBlank()) headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(roomAssignUrl, HttpMethod.POST, entity, Void.class);
            log.info("Room assigned successfully in hostel {} to student {}", hostelId, studentId);
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
                    Object existingHostelObj = (r.getDetails() != null) ? r.getDetails().get("hostelId") : null;
                    String existingHostelId = (existingHostelObj == null) ? null : String.valueOf(existingHostelObj);
                    return hostelId.equals(existingHostelId);
                });
    }


    public List<RequestResponseDTO> getRequestsByHostel(String hostelId) {
        return repository.findAll()
                .stream()
                .filter(request -> request.getDetails().containsKey("hostelId") && request.getDetails().get("hostelId").equals(hostelId))
                .map(RequestMapper::toResponse)
                .collect(Collectors.toList());

    }
}
