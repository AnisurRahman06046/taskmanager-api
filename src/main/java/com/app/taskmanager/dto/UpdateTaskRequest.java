package com.app.taskmanager.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTaskRequest {

    @Size(min = 1, max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    @Pattern(regexp = "TODO|IN_PROGRESS|DONE")
    private String status;
}
