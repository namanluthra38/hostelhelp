package com.hostelhelp.studentservice.dto;

import com.hostelhelp.studentservice.dto.HostelResponseDTO;
import com.hostelhelp.studentservice.dto.RoomResponseDTO;

public record StudentCompositeDTO(
        StudentResponseDTO student,
        RoomResponseDTO room,
        HostelResponseDTO hostel
) {}
