package com.app.taskmanager.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.app.taskmanager.common.dto.PaginateResponse;
import com.app.taskmanager.dto.CreateTaskRequest;
import com.app.taskmanager.dto.TaskResponse;
import com.app.taskmanager.dto.UpdateTaskRequest;
import com.app.taskmanager.entity.Status;
import com.app.taskmanager.entity.Task;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.exception.ForbiddenException;
import com.app.taskmanager.repository.TaskRepository;
import com.app.taskmanager.repository.UserRepository;
import com.app.taskmanager.service.TaskService;
import com.app.taskmanager.specification.TaskSpecification;
import com.app.taskmanager.util.SecurityUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private PaginateResponse<TaskResponse> mapToPageResponse(Page<Task> page) {
        return PaginateResponse.<TaskResponse>builder()
                .data(page.getContent().stream().map(this::mapToResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private Task getTaskForCurrentUser(Long taskId) {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            // throw new RuntimeException("Access Denied!");
            throw new ForbiddenException();
        }
        return task;

    }

    private Status parseStatus(String status) {
        try {
            return Status.valueOf(status);
        } catch (Exception e) {
            throw new RuntimeException("Invalid status");
        }
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .build();
    }

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(Status.valueOf(request.getStatus()))
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    @Override

    public PaginateResponse<TaskResponse> getMyTasks(int page, int size, String sortBy, String status, String title) {

        List<String> allowedSort = List.of("createdAt", "title", "status");

        if (!allowedSort.contains(sortBy)) {
            throw new RuntimeException("Invalid sort field");
        }

        String email = SecurityUtil.getCurrentUserEmail();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<Task> tasks;

        Specification<Task> spec = Specification.where(TaskSpecification.hasUserId(user.getId()))
                .and(TaskSpecification.hasStatus(status)).and(TaskSpecification.hasTitle(title));

        Page<Task> result = taskRepository.findAll(spec, pageable);
        return mapToPageResponse(result);

    }

    @Override
    public TaskResponse updateTask(Long id, UpdateTaskRequest req) {
        Task task = getTaskForCurrentUser(id);
        if (req.getTitle() != null) {

            task.setTitle((req.getTitle()));
        }
        if (req.getDescription() != null) {

            task.setDescription(req.getDescription());
        }
        if (req.getStatus() != null) {

            task.setStatus(Status.valueOf(req.getStatus()));
        }

        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = getTaskForCurrentUser(id);
        taskRepository.delete(task);
    }
}
