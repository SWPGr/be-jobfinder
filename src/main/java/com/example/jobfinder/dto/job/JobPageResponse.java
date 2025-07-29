package com.example.jobfinder.dto.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class JobPageResponse<T> {
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isLast;
    private boolean isFirst;
    private List<T> content;
    private Long totalApplication; // <--- Thêm trường này
    private Long totalOpenJob;
}
