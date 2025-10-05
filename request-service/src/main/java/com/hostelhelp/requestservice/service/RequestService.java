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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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


    public RequestResponseDTO createJoinRequest(CreateRequestDTO dto) {

        boolean exists = repository.findByStudentId(dto.studentId())
                .stream()
                .anyMatch(r -> r.getType() == Request.RequestType.HOSTEL_JOIN && r.getStatus() == Request.Status.PENDING);
        if (exists) {
            throw new IllegalStateException("A pending hostel join request already exists for this student.");
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

        // Step 1: Verify student exists
        String getUrl = "http://localhost:4004/students/" + studentId;
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

        // Step 2: Assign hostel
        String assignUrl = "http://localhost:4004/students/" + studentId + "/assign-hostel";
        var assignHostelDTO = Map.of("hostelId", hostelId);

        try {
            log.info("Assigning hostel {} to student {}", hostelId, studentId);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(assignHostelDTO, headers);

            restTemplate.exchange(assignUrl, HttpMethod.POST, entity, Void.class);
            log.info("Hostel {} assigned successfully to student {}", hostelId, studentId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 403) {
                log.error("Forbidden when calling student-service for student {}: {}", studentId, e.getMessage());
                throw new RuntimeException("Forbidden call to student-service. Check permissions.");
            } else if (e.getStatusCode().value() == 401) {
                log.error("Unauthorized when calling student-service for student {}: {}", studentId, e.getMessage());
                throw new RuntimeException("Unauthorized call to student-service. Check security config.");
            } else {
                log.error("Unexpected error assigning hostel for student {}: {}", studentId, e.getMessage());
                throw new RuntimeException("Unexpected error assigning hostel: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Unexpected error assigning hostel for student {}: {}", studentId, e.getMessage());
            throw new RuntimeException("Unexpected error assigning hostel: " + e.getMessage());
        }
    }
}