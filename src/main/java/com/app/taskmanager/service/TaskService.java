package com.app.taskmanager.service;

import com.app.taskmanager.common.dto.PaginateResponse;
import com.app.taskmanager.dto.CreateTaskRequest;
import com.app.taskmanager.dto.TaskResponse;
import com.app.taskmanager.dto.UpdateTaskRequest;

public interface TaskService {
    TaskResponse createTask(CreateTaskRequest request);

    // List<TaskResponse> getMyTasks();
    PaginateResponse<TaskResponse> getMyTasks(int page, int size, String sortBy, String status, String title);

    TaskResponse updateTask(Long id, UpdateTaskRequest request);

    void deleteTask(Long id);
}
