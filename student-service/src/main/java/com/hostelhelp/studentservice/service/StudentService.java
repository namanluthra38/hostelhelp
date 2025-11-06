package com.hostelhelp.studentservice.service;

import com.hostelhelp.studentservice.dto.AssignRoomDTO;
import com.hostelhelp.studentservice.dto.StudentMinDetailsDTO;
import com.hostelhelp.studentservice.dto.StudentRequestDTO;
import com.hostelhelp.studentservice.dto.StudentResponseDTO;
import com.hostelhelp.studentservice.dto.UpdateStudentDTO;
import com.hostelhelp.studentservice.dto.UserDTO;
import com.hostelhelp.studentservice.exception.EmailAlreadyExistsException;
import com.hostelhelp.studentservice.exception.StudentNotFoundException;
import com.hostelhelp.studentservice.mapper.StudentMapper;
import com.hostelhelp.studentservice.model.Student;
import com.hostelhelp.studentservice.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {
    private final StudentRepository studentRepository;
    private final RestTemplate restTemplate;

    // small ObjectMapper to parse possible JSON-stringified room objects saved in DB
    private final ObjectMapper objectMapper = new ObjectMapper();



    public List<StudentResponseDTO> getStudents() {
        List<Student> students = studentRepository.findAll();

        return students.stream().map(StudentMapper::toDTO).toList();
    }

    public StudentResponseDTO getStudent(UUID id) {
        Student student = studentRepository.findById(id).orElseThrow(() ->
                new StudentNotFoundException("Student not found with id " + id));

        return StudentMapper.toDTO(student);
    }

    public StudentResponseDTO createStudent(StudentRequestDTO studentRequestDTO) {
        if (studentRepository.existsByEmail(studentRequestDTO.email())) {
            throw new EmailAlreadyExistsException(
                    "A student with this email " + "already exists"
                            + studentRequestDTO.email());
        }


        Student newStudent = studentRepository.save(
                StudentMapper.toModel(studentRequestDTO));
        UserDTO userDTO = new UserDTO(newStudent.getEmail(), newStudent.getPassword(), "STUDENT");
        restTemplate.postForObject("http://api-gateway:4004/auth/register", userDTO, Void.class);
        //restTemplate.postForObject("http://localhost:4004/auth/register", userDTO, Void.class);
        return StudentMapper.toDTO(newStudent);
    }

    public StudentResponseDTO updateStudent(UUID id,
                                            StudentRequestDTO studentRequestDTO) {
        Student student = studentRepository.findById(id).orElseThrow(() ->
                new StudentNotFoundException("Student not found with id " + id));

        if(studentRepository.existsByEmailAndIdNot(studentRequestDTO.email(), id)){
            throw new EmailAlreadyExistsException("A student with the email "
                    + studentRequestDTO.email() + " already exists");
        }

        student.setName(studentRequestDTO.name());
        student.setEmail(studentRequestDTO.email());
        student.setAddress(studentRequestDTO.address());
        student.setDateOfBirth(studentRequestDTO.dateOfBirth());
        Student updatedStudent = studentRepository.save(student);
        return StudentMapper.toDTO(updatedStudent);
    }

    public void deleteStudent(UUID id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new StudentNotFoundException("Student not found with id " + id));
        String email = student.getEmail();
        studentRepository.deleteById(id);
        // Delete user in auth-service
        try {
            String deleteUrl = "http://api-gateway:4004/auth/user/" + email;
            restTemplate.delete(deleteUrl);
        } catch (Exception e) {
            log.error("Failed to delete user in auth-service for email: {}", email, e);
        }
    }

    public StudentResponseDTO getStudentByEmail(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with email " + email));
        return StudentMapper.toDTO(student);
    }

    public StudentResponseDTO updateStudentByEmail(String email, UpdateStudentDTO updateStudentDTO) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with email " + email));

        student.setPhone(updateStudentDTO.phone());
        student.setAddress(updateStudentDTO.address());
        student.setDateOfBirth(updateStudentDTO.dateOfBirth());
        Student updatedStudent = studentRepository.save(student);
        return StudentMapper.toDTO(updatedStudent);
    }


    public StudentResponseDTO assignRoom(UUID studentId, AssignRoomDTO dto, String token) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id " + studentId));
        if (student.getHostelId() != null) throw new IllegalArgumentException("Already hostel assigned");

        Boolean isBoysHostel = null;
        try {
            String hostelUrl = "http://api-gateway:4004/hostels/" + dto.hostelId() + "/is-boys";
            if (token == null || token.isBlank()) {
                // If you expect caller always to forward token, treat as auth error:
                throw new IllegalArgumentException("Missing authentication token for verifying hostel");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Boolean> resp = restTemplate.exchange(hostelUrl, HttpMethod.GET, entity, Boolean.class);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new IllegalArgumentException("Failed to verify hostel type: " + resp.getStatusCode());
            }
            isBoysHostel = resp.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Hostel not found: " + dto.hostelId());
        } catch (HttpClientErrorException e) {
            // bubble up a clear message for caller
            log.error("Error calling hostel service for hostel {}: {}", dto.hostelId(), e.getMessage());
            throw new IllegalArgumentException("Failed to verify hostel type: " + e.getStatusCode() + " " + e.getMessage());
        } catch (Exception e) {
            log.error("Error calling hostel service for hostel {}: {}", dto.hostelId(), e.getMessage());
            throw new IllegalArgumentException("Failed to verify hostel type: " + e.getMessage());
        }

        // enforce gender rules when we could determine hostel type
        if (isBoysHostel != null && student.getGender() != null) {
            String g = student.getGender().trim().toLowerCase();
            if ((g.equals("male") || g.equals("m")) && !isBoysHostel) {
                throw new IllegalArgumentException("Cannot assign male student to a girls hostel");
            }
            if ((g.equals("female") || g.equals("f")) && isBoysHostel) {
                throw new IllegalArgumentException("Cannot assign female student to a boys hostel");
            }
        }

        student.setRoomId(dto.roomId());
        student.setHostelId(dto.hostelId());
        studentRepository.save(student);
        log.info("Student {} assigned hostel {} room {}", studentId, dto.hostelId(), dto.roomId());
        return StudentMapper.toDTO(student);
    }


    // New: fetch students belonging to a hostel
    public List<StudentResponseDTO> getStudentsByHostelId(UUID hostelId) {
        if (hostelId == null) return List.of();
        List<Student> students = studentRepository.findByHostelId(hostelId.toString());
        return students.stream().map(StudentMapper::toDTO).toList();
    }
    // New: Fetch hostel object for a student (returns Map or null)
    public Map<String, Object> getHostelForStudent(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id " + studentId));

        String hostelId = student.getHostelId();
        if (hostelId == null || hostelId.isBlank()) {
            return null;
        }

        //String url = "http://localhost:4001/hostels/" + hostelId;
        String url = "http://api-gateway:4004/hostels/" + hostelId;
        try {
            Object resp = restTemplate.getForObject(url, Object.class);
            if (resp == null) return null;
            Map<String, Object> hostel = objectMapper.convertValue(resp, new TypeReference<Map<String, Object>>(){});
            return hostel;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("Hostel not found for id {}", hostelId);
                return null;
            }
            log.error("Error fetching hostel {}: {}", hostelId, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching hostel {}: {}", hostelId, e.getMessage());
            return null;
        }
    }

    // New: Fetch room object for a student (returns Map or null) - simplified and uses RoomController endpoint
    public Map<String, Object> getRoomForStudent(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id " + studentId));

        Object rawRoom = student.getRoomId();
        if (rawRoom == null) {
            return null;
        }

        // Extract roomId from possible representations (string, JSON-string, or object)
        String roomId = null;
        try {
            if (rawRoom instanceof String) {
                String s = ((String) rawRoom).trim();
                if (s.startsWith("{")) {
                    // stored as JSON string
                    Map<String, Object> parsed = objectMapper.readValue(s, new TypeReference<Map<String, Object>>(){});
                    Object val = parsed.getOrDefault("roomId", parsed.get("id"));
                    roomId = (val == null) ? null : String.valueOf(val);
                } else {
                    roomId = s;
                }
            } else {
                // stored as an object-like structure
                Map<String, Object> asMap = objectMapper.convertValue(rawRoom, new TypeReference<Map<String, Object>>(){});
                Object val = asMap.getOrDefault("roomId", asMap.get("id"));
                roomId = (val == null) ? null : String.valueOf(val);
            }
        } catch (Exception ex) {
            log.warn("Failed to extract roomId for student {}: {}", studentId, ex.getMessage());
            // fall through and return null below
        }

        if (roomId == null || roomId.isBlank()) return null;

        // Call the RoomController endpoint (single canonical source)
        //String url = "http://localhost:4001/hostels/rooms/" + roomId;
        String url = "http://api-gateway:4004/hostels/rooms/" + roomId;
        try {
            Object resp = restTemplate.getForObject(url, Object.class);
            if (resp == null) return null;
            return objectMapper.convertValue(resp, new TypeReference<Map<String, Object>>(){});
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("Room not found for id {}", roomId);
                return null;
            }
            log.error("Error fetching room {}: {}", roomId, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching room {}: {}", roomId, e.getMessage());
            return null;
        }
    }

    public StudentResponseDTO leaveHostel(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id " + studentId));
        student.setRoomId(null);
        student.setHostelId(null);
        studentRepository.save(student);
        return StudentMapper.toDTO(student);
    }

    public StudentMinDetailsDTO getStudentMinDetails(UUID id) {
        Student student = studentRepository.findById(id).orElseThrow(() ->
                new StudentNotFoundException("Student not found with id " + id));


        return StudentMapper.toMinDetails(student);
    }
}
