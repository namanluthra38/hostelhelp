package com.hostelhelp.studentservice.controller;

import com.hostelhelp.studentservice.dto.AssignRoomDTO;
import com.hostelhelp.studentservice.dto.StudentMinDetailsDTO;
import com.hostelhelp.studentservice.dto.StudentRequestDTO;
import com.hostelhelp.studentservice.dto.StudentResponseDTO;
import com.hostelhelp.studentservice.dto.UpdateStudentDTO;
import com.hostelhelp.studentservice.exception.StudentNotFoundException;
import com.hostelhelp.studentservice.service.StudentService;
import com.hostelhelp.studentservice.validation.CreateStudentValidationGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/students")
@Tag(name = "Student", description = "API for managing Students")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "Get all students")
    @PreAuthorize("hasAnyRole('ADMIN', 'WARDEN')")
    public ResponseEntity<List<StudentResponseDTO>> getStudents() {
        List<StudentResponseDTO> students = studentService.getStudents();
        return ResponseEntity.ok().body(students);
    }



    @GetMapping("/{id}")
    @Operation(summary = "Get a student by ID")
    @PreAuthorize("hasAnyRole('ADMIN','WARDEN')")
    public ResponseEntity<StudentResponseDTO> getStudent(@PathVariable UUID id) {
        try {
            StudentResponseDTO student = studentService.getStudent(id);
            return ResponseEntity.ok().body(student);
        } catch (StudentNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/name")
    @PreAuthorize("hasAnyRole('STUDENT','WARDEN','ADMIN')")
    public ResponseEntity<String> getNameById(@PathVariable UUID id){
        try {
            var dto = studentService.getStudentMinDetails(id);
            if (dto == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok().body(dto.name());
        } catch (StudentNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/min")
    @PreAuthorize("hasAnyRole('STUDENT','WARDEN','ADMIN')")
    public ResponseEntity<StudentMinDetailsDTO> getMinDetailsById(@PathVariable UUID id) {
        try {
            StudentMinDetailsDTO dto = studentService.getStudentMinDetails(id);
            return ResponseEntity.ok(dto);
        } catch (StudentNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Create a new student")
    public ResponseEntity<StudentResponseDTO> createStudent(
            @Validated({Default.class, CreateStudentValidationGroup.class})
            @RequestBody StudentRequestDTO studentRequestDTO) {
        StudentResponseDTO studentResponseDTO = studentService.createStudent(studentRequestDTO);
        return ResponseEntity.ok().body(studentResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a student")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponseDTO> updateStudent(
            @PathVariable UUID id,
            @Validated(Default.class) @RequestBody StudentRequestDTO studentRequestDTO) {
        StudentResponseDTO studentResponseDTO = studentService.updateStudent(id, studentRequestDTO);
        return ResponseEntity.ok().body(studentResponseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a student")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable UUID id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current student's profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentResponseDTO> getCurrentStudentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        StudentResponseDTO student = studentService.getStudentByEmail(email);
        return ResponseEntity.ok().body(student);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current student's profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentResponseDTO> updateCurrentStudentProfile(
            @Validated(Default.class) @RequestBody UpdateStudentDTO updateStudentDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        StudentResponseDTO student = studentService.updateStudentByEmail(email, updateStudentDTO);
        return ResponseEntity.ok().body(student);
    }

    @PostMapping("/{studentId}/assign-room")
    @PreAuthorize("hasAnyRole('ADMIN','WARDEN')")
    public ResponseEntity<?> assignRoom(
            @PathVariable UUID studentId,
            @RequestBody AssignRoomDTO dto,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            String token = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
            StudentResponseDTO updatedStudent = studentService.assignRoom(studentId, dto, token);
            return ResponseEntity.ok(updatedStudent);
        } catch (StudentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error assigning room for student {}: {}", studentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to assign room");
        }
    }

    @PostMapping("/{studentId}/leave")
    @Operation(summary = "Assign a room to a student")
    @PreAuthorize("hasAnyRole('ADMIN', 'WARDEN')")
    public ResponseEntity<StudentResponseDTO> leaveHostel(
            @PathVariable UUID studentId
    ) {
        try{
            StudentResponseDTO updatedStudent = studentService.leaveHostel(studentId);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

    }

    @GetMapping("/hostel/{hostelId}")
    @Operation(summary = "Get students by hostel id")
    @PreAuthorize("hasAnyRole('WARDEN','ADMIN')")
    public ResponseEntity<List<StudentResponseDTO>> getStudentsByHostel(@PathVariable UUID hostelId) {
        List<StudentResponseDTO> students = studentService.getStudentsByHostelId(hostelId);
        return ResponseEntity.ok().body(students);
    }


}
