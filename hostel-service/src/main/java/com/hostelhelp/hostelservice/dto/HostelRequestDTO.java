package com.hostelhelp.hostelservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HostelRequestDTO {

    @NotNull(message = "Hostel name cannot be null")
    private String name;

    @NotNull(message = "AC availability must be specified")
    private Boolean hasAC;

    @Min(value = 1, message = "Number of rooms must be at least 1")
    @Max(value = 1000, message = "Number of rooms cannot exceed 1000")
    private Integer numberOfRooms;

    @Min(value = 1, message = "Seats per room must be at least 1")
    @Max(value = 10, message = "Seats per room cannot exceed 10")
    private Integer numberOfSeatsPerRoom;

    @NotNull(message = "Hostel type (Boys/Girls) must be specified")
    private Boolean isBoysHostel;
}
