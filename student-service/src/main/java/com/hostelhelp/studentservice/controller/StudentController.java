package com.hostelhelp.studentservice.controller;

import com.hostelhelp.studentservice.dto.AssignRoomDTO;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponseDTO> getStudent(@PathVariable UUID id) {
        try {
            StudentResponseDTO student = studentService.getStudent(id);
            return ResponseEntity.ok().body(student);
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
    @Operation(summary = "Assign a room to a student")
    @PreAuthorize("hasAnyRole('ADMIN', 'WARDEN')")
    public ResponseEntity<StudentResponseDTO> assignRoom(
            @PathVariable UUID studentId,
            @RequestBody AssignRoomDTO dto
    ) {
        StudentResponseDTO updatedStudent = studentService.assignRoom(studentId, dto);
        return ResponseEntity.ok(updatedStudent);
    }

}
