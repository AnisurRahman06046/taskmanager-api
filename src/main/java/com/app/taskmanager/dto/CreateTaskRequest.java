package com.app.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateTaskRequest {
    @NotBlank
    private String title;
    private String description;
    @Pattern(regexp="TODO|IN_PROGRESS|DONE")
    private String status;
}
