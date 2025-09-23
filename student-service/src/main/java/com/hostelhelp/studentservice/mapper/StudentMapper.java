package com.hostelhelp.studentservice.mapper;

import com.hostelhelp.studentservice.dto.StudentRequestDTO;
import com.hostelhelp.studentservice.dto.StudentResponseDTO;
import com.hostelhelp.studentservice.model.Student;

public class StudentMapper {

    public static Student toModel(StudentRequestDTO dto) {
        return Student.builder()
                .name(dto.name())
                .email(dto.email())
                .password(dto.password())
                .graduationYear(dto.graduationYear())
                .uid(dto.uid())
                .address(dto.address())
                .dateOfBirth(dto.dateOfBirth())
                .gender(dto.gender())
                .phone(dto.phone())
                .hostelId(dto.hostelId())
                .roomNumber(dto.roomNumber())
                .build();
    }

    public static StudentResponseDTO toDTO(Student student) {
        return new StudentResponseDTO(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getGraduationYear(),
                student.getUid(),
                student.getAddress(),
                student.getDateOfBirth(),
                student.getGender(),
                student.getPhone(),
                student.getHostelId(),
                student.getRoomNumber(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }
}
