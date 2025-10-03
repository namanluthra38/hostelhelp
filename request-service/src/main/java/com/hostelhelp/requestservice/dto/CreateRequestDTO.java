package com.hostelhelp.requestservice.dto;

import com.hostelhelp.requestservice.model.Request.RequestType;

import java.util.Map;

public record CreateRequestDTO(
        String studentId,
        RequestType type,
        Map<String, Object> details
) {}
