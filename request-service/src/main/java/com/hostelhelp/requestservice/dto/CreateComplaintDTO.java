package com.hostelhelp.requestservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class CreateComplaintDTO {


    @NotBlank
    @Size(max = 150)
    private String title;

    @NotBlank
    @Size(max = 4000)
    private String description;

    private List<String> attachments;

}
