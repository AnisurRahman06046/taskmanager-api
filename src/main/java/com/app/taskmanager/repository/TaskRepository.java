package com.app.taskmanager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.app.taskmanager.entity.Status;
import com.app.taskmanager.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Page<Task> findByUserId(Long userId, Pageable pageable);

    Page<Task> findByUserIdAndStatus(Long userId, Status status, Pageable pageable);
}