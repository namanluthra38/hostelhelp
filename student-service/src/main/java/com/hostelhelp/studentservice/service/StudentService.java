package com.hostelhelp.studentservice.service;

import com.hostelhelp.studentservice.dto.StudentRequestDTO;
import com.hostelhelp.studentservice.dto.StudentResponseDTO;
import com.hostelhelp.studentservice.dto.UserDTO;
import com.hostelhelp.studentservice.exception.EmailAlreadyExistsException;
import com.hostelhelp.studentservice.exception.StudentNotFoundException;
import com.hostelhelp.studentservice.mapper.StudentMapper;
import com.hostelhelp.studentservice.model.Student;
import com.hostelhelp.studentservice.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final RestTemplate restTemplate;

    public StudentService(StudentRepository studentRepository, RestTemplate restTemplate) {
        this.studentRepository = studentRepository;
        this.restTemplate = restTemplate;
    }


    public List<StudentResponseDTO> getStudents() {
        List<Student> students = studentRepository.findAll();

        return students.stream().map(StudentMapper::toDTO).toList();

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
        if (!studentRepository.existsById(id)) {
            throw new StudentNotFoundException("Student not found with id " + id);
        }
        studentRepository.deleteById(id);
    }
}
