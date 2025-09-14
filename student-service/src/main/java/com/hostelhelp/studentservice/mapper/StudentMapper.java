package com.hostelhelp.studentservice.mapper;

import com.hostelhelp.studentservice.dto.StudentRequestDTO;
import com.hostelhelp.studentservice.dto.StudentResponseDTO;
import com.hostelhelp.studentservice.model.Student;

import java.time.LocalDate;

public class StudentMapper {

    public static StudentResponseDTO toDTO(Student student) {
        StudentResponseDTO dto = new StudentResponseDTO();
        dto.setId(student.getId().toString());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        dto.setGraduationYear(student.getGraduationYear());
        dto.setUID(student.getUID());
        dto.setAddress(student.getAddress());
        dto.setDateOfBirth(student.getDateOfBirth().toString());
        dto.setGender(student.getGender());
        dto.setPhone(student.getPhone());
        dto.setHostel(student.getHostel());
        dto.setRoomNumber(student.getRoomNumber());

        return dto;
    }

    public static Student toModel(StudentRequestDTO requestDTO) {
        Student student = new Student();
        student.setName(requestDTO.getName());
        student.setEmail(requestDTO.getEmail());
        student.setPassword(requestDTO.getPassword());
        student.setGraduationYear(requestDTO.getGraduationYear());
        student.setUID(requestDTO.getUid());
        student.setAddress(requestDTO.getAddress());
        student.setDateOfBirth(LocalDate.parse(requestDTO.getDateOfBirth()));
        student.setGender(requestDTO.getGender());
        student.setPhone(requestDTO.getPhone());
        student.setHostel(requestDTO.getHostel());
        student.setRoomNumber(requestDTO.getRoomNumber());

        return student;
    }
}
