package com.hostelhelp.hostelservice.dto;

import lombok.Data;

@Data
public class HostelResponseDTO {
    private String id;
    private String name;
    private Boolean hasAC;
    private Integer numberOfRooms;
    private Integer numberOfSeatsPerRoom;
    private Boolean isBoysHostel;
    private String createdAt;
    private String updatedAt;
}
