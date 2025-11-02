package com.hostelhelp.studentservice.repository;

import com.hostelhelp.studentservice.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    Optional<Student> findByEmail(String email);
    List<Student> findByHostelId(String hostelId);
}
