package com.hostelhelp.studentservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    private String name;
    @NotNull
    @Email
    @Column(unique = true)
    private String email;
    @NotNull
    private String password;
    @NotNull
    private Integer graduationYear;
    @NotNull
    @Column(unique = true)
    private String UID;
    @NotNull
    private String address;
    @NotNull
    private LocalDate dateOfBirth;
    @NotNull
    private String gender;
    @NotNull
    private String phone;
    private String hostel;
    private Integer roomNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }



}
