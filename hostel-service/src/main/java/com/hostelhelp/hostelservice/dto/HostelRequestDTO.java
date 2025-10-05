package com.hostelhelp.hostelservice.dto;

import com.hostelhelp.hostelservice.validation.CreateHostelValidationGroup;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record HostelRequestDTO(
        UUID id,

        @NotBlank(groups = {CreateHostelValidationGroup.class}, message = "Name is required")
        String name,

        @NotNull(message = "hasAC field is required")
        Boolean hasAC,

        @NotNull(message = "Number of rooms is required")
        @Min(value = 1, message = "Number of rooms must be at least 1")
        @Max(value = 1000, message = "Number of rooms cannot exceed 1000")
        Integer numberOfRooms,



        @NotNull(message = "Charges per semester is required")
        @Positive(message = "Charges per semester must be positive")
        Double chargesPerSemester,

        @NotNull(message = "isBoysHostel field is required")
        Boolean isBoysHostel
) {}
