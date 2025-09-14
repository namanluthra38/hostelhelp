package com.hostelhelp.hostelservice.mapper;

import com.hostelhelp.hostelservice.dto.HostelRequestDTO;
import com.hostelhelp.hostelservice.dto.HostelResponseDTO;
import com.hostelhelp.hostelservice.model.Hostel;

public class HostelMapper {

    public static HostelResponseDTO toDTO(Hostel hostel) {
        HostelResponseDTO dto = new HostelResponseDTO();
        dto.setId(hostel.getId().toString());
        dto.setName(hostel.getName());
        dto.setHasAC(hostel.isHasAC());
        dto.setNumberOfRooms(hostel.getNumberOfRooms());
        dto.setNumberOfSeatsPerRoom(hostel.getNumberOfSeatsPerRoom());
        dto.setIsBoysHostel(hostel.isBoysHostel());
        dto.setCreatedAt(hostel.getCreatedAt().toString());
        dto.setUpdatedAt(hostel.getUpdatedAt().toString());
        return dto;
    }

    public static Hostel toModel(HostelRequestDTO dto) {
        return Hostel.builder()
                .name(dto.getName())
                .hasAC(dto.getHasAC())
                .numberOfRooms(dto.getNumberOfRooms())
                .numberOfSeatsPerRoom(dto.getNumberOfSeatsPerRoom())
                .isBoysHostel(dto.getIsBoysHostel())
                .build();
    }
}
