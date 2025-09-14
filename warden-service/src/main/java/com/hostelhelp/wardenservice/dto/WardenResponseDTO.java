package com.hostelhelp.wardenservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class WardenResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private String gender;
    private String phone;
    private String hostel;
    private Integer roomNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
