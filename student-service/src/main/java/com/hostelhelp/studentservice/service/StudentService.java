package com.hostelhelp.studentservice.service;

import com.hostelhelp.studentservice.dto.AssignRoomDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

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
        //restTemplate.postForObject("http://api-gateway:4004/auth/register", userDTO, Void.class);
        restTemplate.postForObject("http://localhost:4004/auth/register", userDTO, Void.class);
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
            System.err.println("Failed to delete user in auth-service for email: " + email);
            e.printStackTrace();
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


    public StudentResponseDTO assignRoom(UUID studentId, AssignRoomDTO dto) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id " + studentId));
        student.setRoomId(dto.roomId());
        student.setHostelId(dto.hostelId());
        studentRepository.save(student);
        return StudentMapper.toDTO(student);
    }

    // New: Fetch hostel object for a student (returns Map or null)
    public Map<String, Object> getHostelForStudent(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id " + studentId));

        String hostelId = student.getHostelId();
        if (hostelId == null || hostelId.isBlank()) {
            return null;
        }

        String url = "http://localhost:4001/hostels/" + hostelId;
        try {
            Map<String, Object> hostel = restTemplate.getForObject(url, Map.class);
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

    // New: Fetch room object for a student (returns Map or null)
    public Map<String, Object> getRoomForStudent(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id " + studentId));

        Object rawRoom = student.getRoomId();
        String roomId = null;
        if (rawRoom == null) {
            return null;
        }
        if (rawRoom instanceof String) {
            String s = (String) rawRoom;
            // handle case where DB stored a JSON-stringified object
            if (s.trim().startsWith("{")) {
                try {
                    Map parsed = objectMapper.readValue(s, Map.class);
                    Object val = parsed.getOrDefault("roomId", parsed.get("id"));
                    roomId = val == null ? null : String.valueOf(val);
                } catch (Exception ex) {
                    log.warn("Failed to parse roomId string for student {}: {}", studentId, ex.getMessage());
                    roomId = s; // fallback to raw
                }
            } else {
                roomId = s;
            }
        } else {
            // if stored as object, try to extract common fields
            try {
                Map asMap = objectMapper.convertValue(rawRoom, Map.class);
                Object val = asMap.getOrDefault("roomId", asMap.get("id"));
                roomId = val == null ? null : String.valueOf(val);
            } catch (Exception ex) {
                roomId = String.valueOf(rawRoom);
            }
        }

        if (roomId == null || roomId.isBlank()) return null;

        // Try two room endpoints: /hostels/rooms/{roomId} and /rooms/{roomId}
        String url1 = "http://localhost:4001/hostels/rooms/" + roomId;
        String url2 = "http://localhost:4001/rooms/" + roomId;
        try {
            Map<String, Object> room = restTemplate.getForObject(url1, Map.class);
            return room;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                // try second endpoint
                try {
                    Map<String, Object> room = restTemplate.getForObject(url2, Map.class);
                    return room;
                } catch (HttpClientErrorException e2) {
                    if (e2.getStatusCode() == HttpStatus.NOT_FOUND) {
                        log.info("Room not found for id {}", roomId);
                        return null;
                    }
                    log.error("Error fetching room {}: {}", roomId, e2.getMessage());
                    return null;
                } catch (Exception ex) {
                    log.error("Unexpected error fetching room {}: {}", roomId, ex.getMessage());
                    return null;
                }
            }
            log.error("Error fetching room {}: {}", roomId, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching room {}: {}", roomId, e.getMessage());
            return null;
        }
    }
}
