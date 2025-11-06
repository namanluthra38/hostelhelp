package com.hostelhelp.hostelservice.service;

import com.hostelhelp.hostelservice.dto.AssignRoomDTO;
import com.hostelhelp.hostelservice.exception.NoVacantRoomException;
import com.hostelhelp.hostelservice.exception.RemoteServiceException;
import com.hostelhelp.hostelservice.exception.StudentNotFoundRemoteException;
import com.hostelhelp.hostelservice.model.Room;
import com.hostelhelp.hostelservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RestTemplate restTemplate;
    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RoomService.class);

    // Create room with automatic numbering starting from 101
    public Room createRoom(Room room) {
        List<Room> hostelRooms = roomRepository.findByHostelId(room.getHostelId());

        int nextRoomNumber = 101; // default starting number
        if (!hostelRooms.isEmpty()) {
            // Find the max room number for this hostel and increment by 1
            nextRoomNumber = hostelRooms.stream()
                    .map(Room::getRoomNumber)
                    .max(Comparator.naturalOrder())
                    .orElse(100) + 1;
        }

        room.setRoomNumber(nextRoomNumber);
        return roomRepository.save(room);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    // New: fetch all rooms for a given hostelId
    public List<Room> getRoomsByHostelId(UUID hostelId) {
        return roomRepository.findByHostelId(hostelId);
    }

    public Room getRoomById(UUID roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    public void deleteRoom(UUID roomId) {
        roomRepository.deleteById(roomId);
    }

    // Allocate student to first available room in the hostel
    public Room allocateStudent(UUID hostelId, UUID studentId, String token) {
        List<Room> rooms = roomRepository.findByHostelId(hostelId);
        log.debug("Found {} rooms for hostel {}", rooms.size(), hostelId);

        for (Room room : rooms) {
            if (room.hasVacancy()) {
                room.addStudent(studentId);
                Room savedRoom = roomRepository.save(room);
                log.info("Reserved room {} for student {} (hostel {})", savedRoom.getId(), studentId, hostelId);

                // Prepare assign-room REST call
                try {
                    String studentServiceUrl = "http://api-gateway:4004/students/" + studentId + "/assign-room";
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    if (token != null && !token.isBlank()) headers.setBearerAuth(token);

                    AssignRoomDTO dto = new AssignRoomDTO(hostelId.toString(), savedRoom.getId().toString());
                    HttpEntity<AssignRoomDTO> entity = new HttpEntity<>(dto, headers);

                    ResponseEntity<Void> response = restTemplate.postForEntity(studentServiceUrl, entity, Void.class);
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        log.error("Student service assign-room returned non-2xx: {} for student {}", response.getStatusCode(), studentId);
                        throw new RemoteServiceException("Failed to update student record: status " + response.getStatusCode());
                    }

                    log.info("Student {} assigned to room {} successfully (remote updated)", studentId, savedRoom.getId());
                    return savedRoom;
                } catch (HttpClientErrorException.NotFound e) {
                    log.error("Student service responded 404 for student {}: {}", studentId, e.getMessage());
                    // Undo local assignment before throwing
                    room.removeStudent(studentId);
                    roomRepository.save(room);
                    throw new StudentNotFoundRemoteException("Student not found: " + studentId);
                } catch (HttpClientErrorException e) {
                    log.error("Student service client error: {} body: {}", e.getStatusCode(), e.getResponseBodyAsString());
                    room.removeStudent(studentId);
                    roomRepository.save(room);
                    throw new RemoteServiceException("Student service error: " + e.getMessage(), e);
                } catch (Exception e) {
                    log.error("Unexpected error calling student service: {}", e.getMessage(), e);
                    room.removeStudent(studentId);
                    roomRepository.save(room);
                    throw new RemoteServiceException("Failed to notify student service: " + e.getMessage(), e);
                }
            }
        }

        log.warn("No vacant rooms found for hostel {}", hostelId);
        throw new NoVacantRoomException("No vacant rooms in hostel: " + hostelId);
    }

    public Room removeStudent(UUID studentId, UUID roomId, String token) {
        try{
            Room room = getRoomById(roomId);
            room.getStudentIds().remove(studentId);
            return roomRepository.save(room);
        } catch (Exception e) {
            throw new RuntimeException("Room not found");
        }

    }
}
