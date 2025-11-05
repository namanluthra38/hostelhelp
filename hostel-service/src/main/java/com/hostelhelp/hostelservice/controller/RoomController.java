package com.hostelhelp.hostelservice.controller;

import com.hostelhelp.hostelservice.dto.RoomResponseDTO;
import com.hostelhelp.hostelservice.mapper.RoomMapper;
import com.hostelhelp.hostelservice.model.Room;
import com.hostelhelp.hostelservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("hostels/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomMapper roomMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponseDTO> createRoom(@RequestBody Room room) {
        Room createdRoom = roomService.createRoom(room);
        return ResponseEntity.ok(roomMapper.toResponseDTO(createdRoom));
    }

    @GetMapping
    // allow public access during development so frontend can fetch room objects by id
    public ResponseEntity<List<RoomResponseDTO>> getAllRooms() {
        List<RoomResponseDTO> rooms = roomService.getAllRooms()
                .stream()
                .map(roomMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/hostel/{hostelId}")
    // public access - returns rooms belonging to a hostel
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByHostel(@PathVariable UUID hostelId) {
        List<RoomResponseDTO> rooms = roomService.getRoomsByHostelId(hostelId)
                .stream()
                .map(roomMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}")
    // allow public access during development
    public ResponseEntity<RoomResponseDTO> getRoom(@PathVariable UUID roomId) {
        Room room = roomService.getRoomById(roomId);
        return ResponseEntity.ok(roomMapper.toResponseDTO(room));
    }

    // New endpoint: return only the room number for a given room id
    @GetMapping("/{roomId}/number")
    // allow public access
    public ResponseEntity<?> getRoomNumber(@PathVariable UUID roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("roomNumber", room.getRoomNumber()));
    }

    @DeleteMapping("/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    // Allocate student
    @PostMapping("/allocate")
    @PreAuthorize("hasAnyRole('WARDEN','ADMIN')")
    public ResponseEntity<RoomResponseDTO> allocateStudent(
            @RequestParam UUID hostelId,
            @RequestParam UUID studentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // Safely extract token (if present). Do not throw locally; let service throw if required.
        String token = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
        Room updatedRoom = roomService.allocateStudent(hostelId, studentId, token);
        return ResponseEntity.ok(roomMapper.toResponseDTO(updatedRoom));
    }

    @PostMapping("/remove-student")
    @PreAuthorize("hasAnyRole('WARDEN','ADMIN')")
    public ResponseEntity<RoomResponseDTO> removeStudent(
            @RequestParam UUID studentId,
            @RequestParam UUID roomId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String token = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
        Room updatedRoom = roomService.removeStudent(studentId, roomId, token);
        return ResponseEntity.ok(roomMapper.toResponseDTO(updatedRoom));
    }
}
