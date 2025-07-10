package com.example.jobfinder.dto.job;

import lombok.Data;

@Data
public class JobSearchRequest {
    private String keyword;
    private String location;
    private Long categoryId;
    private Long jobLevelId;
    private Long jobTypeId;
    private Long educationId;

    private Integer page = 0;
    private Integer size = 10;

}

