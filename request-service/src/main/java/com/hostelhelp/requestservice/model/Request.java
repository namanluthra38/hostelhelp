package com.hostelhelp.requestservice.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "requests")
public class Request {

    @Id
    private String id;

    private String studentId;

    private String hostelId;

    private RequestType type;

    private Map<String, Object> details;

    private Status status;

    private String reviewedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    public enum RequestType {
        HOSTEL_JOIN, HOSTEL_LEAVE, HOSTEL_CHANGE, ROOM_CHANGE
    }
}
