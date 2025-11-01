package com.hostelhelp.wardenservice.dto;

import com.hostelhelp.wardenservice.dto.HostelResponseDTO;

public record WardenCompositeDTO(
        WardenResponseDTO warden,
        HostelResponseDTO hostel
) {}

