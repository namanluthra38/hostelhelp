package com.hostelhelp.wardenservice.mapper;

import com.hostelhelp.wardenservice.dto.WardenRequestDTO;
import com.hostelhelp.wardenservice.dto.WardenResponseDTO;
import com.hostelhelp.wardenservice.model.Warden;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class WardenMapper {
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public static Warden toModel(WardenRequestDTO dto) {
        return Warden.builder()
                .name(dto.name())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .gender(dto.gender())
                .phone(dto.phone())
                .hostel(dto.hostel())
                .roomNumber(dto.roomNumber())
                .build();
    }

    public static WardenResponseDTO toDTO(Warden warden) {
        return new WardenResponseDTO(
                warden.getId(),
                warden.getName(),
                warden.getEmail(),
                warden.getGender(),
                warden.getPhone(),
                warden.getHostel(),
                warden.getRoomNumber(),
                warden.getCreatedAt(),
                warden.getUpdatedAt()
        );
    }
}
