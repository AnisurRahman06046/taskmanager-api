package com.app.taskmanager.dto;

import lombok.Data;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private String status;
}
