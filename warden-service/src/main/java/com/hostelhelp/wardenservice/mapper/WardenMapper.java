package com.hostelhelp.wardenservice.mapper;

import com.hostelhelp.wardenservice.dto.WardenRequestDTO;
import com.hostelhelp.wardenservice.dto.WardenResponseDTO;
import com.hostelhelp.wardenservice.model.Warden;

public class WardenMapper {

    public static Warden toModel(WardenRequestDTO dto) {
        return Warden.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .gender(dto.getGender())
                .phone(dto.getPhone())
                .hostel(dto.getHostel())
                .roomNumber(dto.getRoomNumber())
                .build();
    }

    public static WardenResponseDTO toDTO(Warden warden) {
        return WardenResponseDTO.builder()
                .id(warden.getId())
                .name(warden.getName())
                .email(warden.getEmail())
                .gender(warden.getGender())
                .phone(warden.getPhone())
                .hostel(warden.getHostel())
                .roomNumber(warden.getRoomNumber())
                .createdAt(warden.getCreatedAt())
                .updatedAt(warden.getUpdatedAt())
                .build();
    }
}
