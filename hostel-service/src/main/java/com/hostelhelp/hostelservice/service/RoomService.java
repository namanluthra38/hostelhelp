package com.hostelhelp.hostelservice.service;

import com.hostelhelp.hostelservice.dto.AssignRoomDTO;
import com.hostelhelp.hostelservice.model.Room;
import com.hostelhelp.hostelservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RestTemplate restTemplate;

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

        for (Room room : rooms) {
            if (room.hasVacancy()) {
                room.addStudent(studentId);
                Room savedRoom = roomRepository.save(room);
                // REST call to student-service to update student's roomId and hostelId
                try {
                    String studentServiceUrl = "http://localhost:4000/students/" + studentId + "/assign-room";
                    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                    headers.setBearerAuth(token);
                    AssignRoomDTO dto = new AssignRoomDTO(hostelId.toString(), savedRoom.getId().toString());
                    org.springframework.http.HttpEntity<AssignRoomDTO> entity = new org.springframework.http.HttpEntity<>(dto, headers);
                    restTemplate.postForEntity(studentServiceUrl, entity, Void.class);
                    System.out.println("Student " + studentId + " assigned to room " + savedRoom.getId());
                } catch (org.springframework.web.client.HttpClientErrorException e) {
                    System.err.println("Failed to update student's roomId in student-service: " + e.getMessage());
                    System.err.println("Response body: " + e.getResponseBodyAsString());
                    throw new RuntimeException("Failed to update student's roomId in student-service: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Failed to update student's roomId in student-service: " + e.getMessage());
                    throw new RuntimeException("Failed to update student's roomId in student-service: " + e.getMessage());
                }
                return savedRoom;
            }
        }
        throw new RuntimeException("No vacant rooms in this hostel");
    }
}
