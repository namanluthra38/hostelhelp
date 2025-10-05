package com.hostelhelp.studentservice.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignRoomDTO(@NotNull String hostelId, @NotNull String roomId) {}
