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
import com.app.taskmanager.exception.BadRequestException;
import com.app.taskmanager.exception.ErrorCode;
import com.app.taskmanager.exception.ResourceNotFoundException;
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

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("createdAt", "title", "status");

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

    private User getCurrentUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private Task getTaskForCurrentUser(Long taskId) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId).orElse(null);
        // Treat "not yours" and "does not exist" identically so attackers
        // cannot enumerate which task IDs are valid by status code alone.
        if (task == null || !task.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException(
                    ErrorCode.TASK_NOT_FOUND,
                    "Task with id '%d' not found".formatted(taskId));
        }
        return task;
    }

    private Status parseStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(ErrorCode.INVALID_STATUS,
                    "Invalid status '%s'. Allowed values: %s".formatted(
                            status, List.of(Status.values())));
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
        User user = getCurrentUser();

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(parseStatus(request.getStatus()))
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    @Override
    public PaginateResponse<TaskResponse> getMyTasks(int page, int size, String sortBy, String status, String title) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException(ErrorCode.INVALID_SORT_FIELD,
                    "Invalid sort field '%s'. Allowed values: %s".formatted(sortBy, ALLOWED_SORT_FIELDS));
        }
        // Validate status early so callers get a 400, not a 500 from the spec.
        parseStatus(status);

        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        Specification<Task> spec = Specification.where(TaskSpecification.hasUserId(user.getId()))
                .and(TaskSpecification.hasStatus(status))
                .and(TaskSpecification.hasTitle(title));

        Page<Task> result = taskRepository.findAll(spec, pageable);
        return mapToPageResponse(result);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest req) {
        Task task = getTaskForCurrentUser(id);
        if (req.getTitle() != null) {
            task.setTitle(req.getTitle());
        }
        if (req.getDescription() != null) {
            task.setDescription(req.getDescription());
        }
        if (req.getStatus() != null) {
            task.setStatus(parseStatus(req.getStatus()));
        }
        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Task task = getTaskForCurrentUser(id);
        taskRepository.delete(task);
    }
}
