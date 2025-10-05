package com.hostelhelp.hostelservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID hostelId; // reference to Hostel


    private Integer roomNumber;

    @Column(nullable = false)
    private Integer totalSeats;

    @ElementCollection
    private List<UUID> studentIds = new ArrayList<>();

    public int getFilledSeats() {
        return studentIds.size();
    }

    public boolean hasVacancy() {
        return studentIds.size() < totalSeats;
    }

    public void addStudent(UUID studentId) {
        if (hasVacancy()) {
            studentIds.add(studentId);
        } else {
            throw new RuntimeException("Room is full");
        }
    }
}
