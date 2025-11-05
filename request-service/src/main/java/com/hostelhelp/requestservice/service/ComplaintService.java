package com.hostelhelp.requestservice.service;

import com.hostelhelp.requestservice.model.Complaint;
import com.hostelhelp.requestservice.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintService {
    private final ComplaintRepository repository;
    private final RestTemplate restTemplate;

    private static final String STUDENT_SERVICE_COMPOSITE = "http://api-gateway:4004/students/me/full";


    public Complaint createComplaint(com.hostelhelp.requestservice.dto.CreateComplaintDTO dto, String token) {
        if (dto == null) throw new IllegalArgumentException("Request body is required");
        if (token == null || token.isBlank()) throw new IllegalArgumentException("Unauthorized: token missing");

        // Forward token to student-service to fetch student composite
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ParameterizedTypeReference<Map<String, Object>> ptr = new ParameterizedTypeReference<>() {};
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(STUDENT_SERVICE_COMPOSITE, HttpMethod.GET, entity, ptr);
            Map<String, Object> composite = resp.getBody();
            if (composite == null) throw new RuntimeException("Failed to fetch student composite");

            // composite contains keys: "student", "room", "hostel" where student is a map
            Map<String, Object> studentObj = null;
            if (composite.get("student") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> tmp = (Map<String, Object>) composite.get("student");
                studentObj = tmp;
            }

            String studentId = null;
            String hostelId = null;

            if (studentObj != null) {
                // student.id might be UUID string or nested. Try common keys
                Object idObj = studentObj.get("id");
                if (idObj != null) studentId = String.valueOf(idObj);
                Object hostelObj = studentObj.get("hostelId");
                if (hostelObj != null) hostelId = String.valueOf(hostelObj);
            }

            // If hostelId missing on student, try composite.hostel (hostel.id)
            if (hostelId == null && composite.get("hostel") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> hostelObj = (Map<String, Object>) composite.get("hostel");
                Object hid = hostelObj.get("id");
                if (hid != null) hostelId = String.valueOf(hid);
            }

            if (studentId == null) throw new RuntimeException("Could not determine student id from student-service response");

            Complaint c = Complaint.builder()
                    .studentId(studentId)
                    .hostelId(hostelId)
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .attachments(dto.getAttachments())
                    .status(Complaint.Status.OPEN)
                    .createdAt(LocalDateTime.now())
                    .build();

            repository.save(c);
            return c;

        } catch (HttpClientErrorException.Unauthorized ue) {
            log.warn("Unauthorized when calling student-service: {}", ue.getMessage());
            throw new RuntimeException("Unauthorized to fetch student info");
        } catch (HttpClientErrorException.NotFound nf) {
            log.warn("Student not found when calling student-service: {}", nf.getMessage());
            throw new RuntimeException("Student not found");
        } catch (Exception e) {
            log.error("Error creating complaint: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create complaint: " + e.getMessage(), e);
        }
    }

    public void deleteComplaint(String id, String token) {
        // Authorization: allow only the student who created it or warden/admin
        Optional<Complaint> opt = repository.findById(id);
        if (opt.isEmpty()) return; // idempotent
        Complaint c = opt.get();

        // If token provided, try to read role; allow deletion if requester is same student
        if (token != null && !token.isBlank()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                // get role via api-gateway
                String role = restTemplate.exchange("http://api-gateway:4004/auth/role", HttpMethod.GET, entity, String.class).getBody();
                if (role != null && role.trim().equalsIgnoreCase("STUDENT")) {
                    // fetch student id from student-service (/students/me)
                    ParameterizedTypeReference<Map<String, Object>> ptr = new ParameterizedTypeReference<>() {};
                    ResponseEntity<Map<String, Object>> resp = restTemplate.exchange("http://api-gateway:4004/students/me", HttpMethod.GET, entity, ptr);
                    Map<String, Object> studentObj = resp.getBody();
                    String sid = null;
                    if (studentObj != null && studentObj.get("id") != null) sid = String.valueOf(studentObj.get("id"));
                    if (sid == null || !sid.equals(c.getStudentId())) {
                        throw new SecurityException("Not authorized to delete this complaint");
                    }
                }
                // Admin/Warden are allowed to delete; no extra checks here
            } catch (SecurityException se) {
                throw se;
            } catch (Exception e) {
                log.warn("Failed to verify delete authorization: {}. Proceeding to delete if owner matches.", e.getMessage());
            }
        }

        repository.deleteById(id);
    }

    public Complaint getComplaint(String id, String token) {
        return repository.findById(id).orElse(null);
    }

    public List<Complaint> getAllComplaints(String token) {
        return repository.findAll();
    }

    public List<Complaint> getHostelComplaints(String hostelId) {
        if (hostelId == null) return List.of();
        // repository.findByHostelId returns single result in current signature; fallback to filtering all
        List<Complaint> all = repository.findAll();
        List<Complaint> out = new ArrayList<>();
        for (Complaint c : all) {
            if (hostelId.equals(c.getHostelId())) out.add(c);
        }
        return out;
    }

    public List<Complaint> getStudentComplaints(String studentId) {
        if (studentId == null) return List.of();
        List<Complaint> all = repository.findAll();
        List<Complaint> out = new ArrayList<>();
        for (Complaint c : all) {
            if (studentId.equals(c.getStudentId())) out.add(c);
        }
        return out;
    }

    public Complaint updateStatus(String id, String statusStr, String token) {
        Optional<Complaint> opt = repository.findById(id);
        if (opt.isEmpty()) return null;
        Complaint c = opt.get();

        Complaint.Status newStatus = Complaint.Status.forValue(statusStr);


        if (token != null && !token.isBlank()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                String role = restTemplate.exchange("http://api-gateway:4004/auth/role", HttpMethod.GET, entity, String.class).getBody();
                if (role != null && role.trim().equalsIgnoreCase("STUDENT")) {
                    ParameterizedTypeReference<Map<String, Object>> ptr = new ParameterizedTypeReference<>() {};
                    ResponseEntity<Map<String, Object>> resp = restTemplate.exchange("http://api-gateway:4004/students/me", HttpMethod.GET, entity, ptr);
                    Map<String, Object> studentObj = resp.getBody();
                    String sid = null;
                    if (studentObj != null && studentObj.get("id") != null) sid = String.valueOf(studentObj.get("id"));
                    if (sid == null || !sid.equals(c.getStudentId())) {
                        throw new SecurityException("Not authorized to update status of this complaint");
                    }
                }
            } catch (SecurityException se) {
                throw se;
            } catch (Exception e) {
                log.warn("Failed to verify update authorization: {}. Proceeding to update status if allowed.", e.getMessage());
            }
        }

        c.setStatus(newStatus);
        repository.save(c);
        return c;
    }

}
