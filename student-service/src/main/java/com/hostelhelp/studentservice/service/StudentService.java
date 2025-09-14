package com.hostelhelp.studentservice.service;

import com.hostelhelp.studentservice.dto.StudentRequestDTO;
import com.hostelhelp.studentservice.dto.StudentResponseDTO;
import com.hostelhelp.studentservice.exception.EmailAlreadyExistsException;
import com.hostelhelp.studentservice.exception.StudentNotFoundException;
import com.hostelhelp.studentservice.mapper.StudentMapper;
import com.hostelhelp.studentservice.model.Student;
import com.hostelhelp.studentservice.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }


    public List<StudentResponseDTO> getStudents() {
        List<Student> students = studentRepository.findAll();

        return students.stream().map(StudentMapper::toDTO).toList();

    }

    public StudentResponseDTO createStudent(StudentRequestDTO studentRequestDTO) {
        if (studentRepository.existsByEmail(studentRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "A student with this email " + "already exists"
                            + studentRequestDTO.getEmail());
        }

        Student newStudent = studentRepository.save(
                StudentMapper.toModel(studentRequestDTO));


        return StudentMapper.toDTO(newStudent);
    }

    public StudentResponseDTO updateStudent(UUID id,
                                            StudentRequestDTO studentRequestDTO) {
        Student student = studentRepository.findById(id).orElseThrow(() ->
                new StudentNotFoundException("Student not found with id " + id));

        if(studentRepository.existsByEmailAndIdNot(studentRequestDTO.getEmail(), id)){
            throw new EmailAlreadyExistsException("A student with the email "
                    + studentRequestDTO.getEmail() + " already exists");
        }

        student.setName(studentRequestDTO.getName());
        student.setEmail(studentRequestDTO.getEmail());
        student.setAddress(studentRequestDTO.getAddress());
        student.setDateOfBirth(LocalDate.parse(studentRequestDTO.getDateOfBirth()));
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
