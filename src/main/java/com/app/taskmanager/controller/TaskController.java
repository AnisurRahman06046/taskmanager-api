package com.app.taskmanager.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.taskmanager.common.dto.ApiResponse;
import com.app.taskmanager.common.dto.PaginateResponse;
import com.app.taskmanager.dto.CreateTaskRequest;
import com.app.taskmanager.dto.TaskResponse;
import com.app.taskmanager.dto.UpdateTaskRequest;
import com.app.taskmanager.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor

public class TaskController {

    private final TaskService taskService;

    @PostMapping("/add")
    public ApiResponse<TaskResponse> create(@Valid @RequestBody CreateTaskRequest req) {
        return ApiResponse.<TaskResponse>builder()
                .success(true)
                .message("task is added")
                .data(taskService.createTask(req))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping
    public ApiResponse<PaginateResponse<TaskResponse>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String title
        ) {

        return ApiResponse.<PaginateResponse<TaskResponse>>builder()
                .success(true)
                .message("Tasks fetched")
                .data(taskService.getMyTasks(page, size, sortBy, status,title))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<TaskResponse> update(@PathVariable Long id, @RequestBody UpdateTaskRequest request) {
        return ApiResponse.<TaskResponse>builder()
                .success(true)
                .message("task is updated")
                .data(taskService.updateTask(id, request))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Task is deleted")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
