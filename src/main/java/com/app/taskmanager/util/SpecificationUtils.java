package com.app.taskmanager.util;

import org.springframework.data.jpa.domain.Specification;

public class SpecificationUtils {
    public static <T> Specification<T> likeIgnoreCase(String field, String value) {
        return (root, query, cb) -> {
            if (value == null)
                return null;
            return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
        };
    }
}
