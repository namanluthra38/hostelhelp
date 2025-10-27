package com.hostelhelp.hostelservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO for transferring Room details safely.
 * Matches the Room entity fields but omits logic methods.
 */

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

