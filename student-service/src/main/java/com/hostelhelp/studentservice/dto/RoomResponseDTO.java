package com.hostelhelp.studentservice.dto;

import java.util.List;
import java.util.UUID;

public record RoomResponseDTO(
        UUID id,
        UUID hostelId,
        Integer roomNumber,
        Integer totalSeats,
        List<UUID> studentIds,
        int filledSeats,
        boolean hasVacancy
) {}
