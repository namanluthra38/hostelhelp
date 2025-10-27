package com.hostelhelp.hostelservice.mapper;

import com.hostelhelp.hostelservice.dto.RoomResponseDTO;
import com.hostelhelp.hostelservice.model.Room;
import org.springframework.stereotype.Component;


@Component
public class RoomMapper {

    public RoomResponseDTO toResponseDTO(Room room) {
        if (room == null) return null;

        return new RoomResponseDTO(
                room.getId(),
                room.getHostelId(),
                room.getRoomNumber(),
                room.getTotalSeats(),
                room.getStudentIds(),
                room.getFilledSeats(),
                room.hasVacancy()
        );
    }

    public Room toEntity(RoomResponseDTO dto) {
        if (dto == null) return null;

        return Room.builder()
                .id(dto.id())
                .hostelId(dto.hostelId())
                .roomNumber(dto.roomNumber())
                .totalSeats(dto.totalSeats())
                .studentIds(dto.studentIds())
                .build();
    }
}
