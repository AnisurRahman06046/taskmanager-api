package com.app.taskmanager.specification;

import org.springframework.data.jpa.domain.Specification;

import com.app.taskmanager.entity.Status;
import com.app.taskmanager.entity.Task;
import com.app.taskmanager.util.SpecificationUtils;

public class TaskSpecification {
    public static Specification<Task> hasUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Task> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null)
                return null;
            return cb.equal(root.get("status"), Status.valueOf(status));
        };
    }

    public static Specification<Task> hasTitle(String title) {
        return SpecificationUtils.likeIgnoreCase("title", title);
    }
}
