package com.app.taskmanager.common.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginateResponse<T> {
    private List<T> data;
    private int page;
    private int size;

    private Long totalElements;
    private int totalPages;

    private boolean first;
    private boolean last;

    
}
