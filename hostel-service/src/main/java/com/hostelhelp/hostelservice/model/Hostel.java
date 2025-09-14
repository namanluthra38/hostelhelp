package com.hostelhelp.hostelservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hostel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column(unique = true)
    private String name;

    @NotNull
    private boolean hasAC;

    @Min(value = 1, message = "Number of rooms must be at least 1")
    @Max(value = 1000, message = "Number of rooms cannot exceed 1000")
    private Integer numberOfRooms;

    @Min(value = 1, message = "Number of seats per room must be at least 1")
    @Max(value = 10, message = "Number of seats per room cannot exceed 10")
    private Integer numberOfSeatsPerRoom;

    @NotNull
    @Positive
    private Double chargesPerSemester;

    @NotNull
    private boolean isBoysHostel;
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
