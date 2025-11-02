package com.hostelhelp.requestservice.mapper;

import com.hostelhelp.requestservice.dto.CreateRequestDTO;
import com.hostelhelp.requestservice.dto.RequestResponseDTO;
import com.hostelhelp.requestservice.model.Request;

public class RequestMapper {

    public static Request toEntity(CreateRequestDTO dto) {
        return Request.builder()
                .studentId(dto.studentId())
                .hostelId(dto.hostelId())
                .type(dto.type())
                .details(dto.details())
                .status(Request.Status.PENDING)
                .build();
    }

    public static RequestResponseDTO toResponse(Request request) {
        return new RequestResponseDTO(
                request.getId(),
                request.getStudentId(),
                request.getHostelId(),
                request.getType(),
                request.getDetails(),
                request.getStatus(),
                request.getReviewedBy(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
