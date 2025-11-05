package com.hostelhelp.requestservice.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "complaint")
public class Complaint {

    @Id
    private String id;

    private String studentId;

    private String hostelId;


    private String title;

    private String description;

    private Status status;

    private List<String> attachments;  // urls or storage keys


    @CreatedDate
    private LocalDateTime createdAt;



    public enum Status {
        OPEN,
        IN_PROGRESS,
        RESOLVED,
        UNKNOWN;

        @JsonValue
        public String toValue() {
            return this.name();
        }

        @JsonCreator
        public static Status forValue(String value) {
            if (value == null) return UNKNOWN;
            String v = value.trim().toUpperCase().replace(' ', '_');
            for (Status s : values()) {
                if (s.name().equals(v)) return s;
            }
            // try case-insensitive comparison, and also allow space/underscore variations
            for (Status s : values()) {
                if (s.name().equalsIgnoreCase(value) || s.name().replace('_', ' ').equalsIgnoreCase(value)) return s;
            }
            return UNKNOWN;
        }
    }


}
