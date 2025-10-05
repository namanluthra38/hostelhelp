package com.hostelhelp.hostelservice.mapper;

import com.hostelhelp.hostelservice.dto.HostelRequestDTO;
import com.hostelhelp.hostelservice.dto.HostelResponseDTO;
import com.hostelhelp.hostelservice.model.Hostel;

public class HostelMapper {

    public static Hostel toModel(HostelRequestDTO dto) {
        return Hostel.builder()
                .id(dto.id())
                .name(dto.name())
                .hasAC(dto.hasAC())
                .numberOfRooms(dto.numberOfRooms())
                .chargesPerSemester(dto.chargesPerSemester())
                .isBoysHostel(dto.isBoysHostel())
                .build();
    }

    public static HostelResponseDTO toDTO(Hostel entity) {
        return new HostelResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.isHasAC(),
                entity.getNumberOfRooms(),
                entity.getChargesPerSemester(),
                entity.isBoysHostel(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
