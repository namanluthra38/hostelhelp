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
            } catch (Exception e) {
                log.error("Student not found in student-service during hostel assignment for request {}: {}", id, e.getMessage());
                throw e;
            }
            return RequestMapper.toResponse(request);
        });
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

        log.info("Fetching student with id: {}", studentId);
        String getUrl = "http://localhost:4000/students/" + studentId;
        try {
            log.info("Checking if student exists: {}", studentId);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            restTemplate.exchange(getUrl, HttpMethod.GET, entity, Object.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                log.error("Student not found in student-service: {}", studentId);
                throw new StudentNotFoundRemoteException("Student not found: " + studentId);
            } else if (e.getStatusCode().value() == 403) {
                log.error("Forbidden when fetching student {}: {}", studentId, e.getMessage());
                throw new RuntimeException("Forbidden call to student-service. Check permissions.");
            } else if (e.getStatusCode().value() == 401) {
                log.error("Unauthorized when fetching student {}: {}", studentId, e.getMessage());
                throw new RuntimeException("Unauthorized call to student-service. Check security config.");
            } else {
                log.error("Error fetching student {}: {}", studentId, e.getMessage());
                throw new RuntimeException("Failed to fetch student before assigning hostel: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error fetching student {}: {}", studentId, e.getMessage());
            throw new RuntimeException("Failed to fetch student before assigning hostel: " + e.getMessage());
        }

        log.info("assigning hostel with id: {}", hostelId);
        String roomAssignUrl = "http://localhost:4001/hostels/rooms/allocate?hostelId=" + hostelId + "&studentId=" + studentId;
        try {
            log.info("Assigning room in hostel {} to student {} via RoomController", hostelId, studentId);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(roomAssignUrl, HttpMethod.POST, entity, Void.class);
            log.info("Room assigned successfully in hostel {} to student {}", hostelId, studentId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                log.error("Room assignment failed: student or hostel not found for student {} in hostel {}", studentId, hostelId);
            } else if (e.getStatusCode().value() == 403) {
                log.error("Forbidden when assigning room for student {}: {}", studentId, e.getMessage());
            } else {
                log.error("Unexpected error assigning room for student {}: {}", studentId, e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error assigning room for student {}: {}", studentId, e.getMessage());
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
}