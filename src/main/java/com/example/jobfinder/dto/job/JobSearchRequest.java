package com.example.jobfinder.dto.job;

import lombok.Data;

@Data
public class JobSearchRequest {
    private String keyword;
    private String location;
    private Long categoryId;
    private Long jobLevelId;
    private Long jobTypeId;
    private Float salaryMin;
    private Float salaryMax;
    private Long educationId;
    private String sort;
    private Boolean salaryNegotiable;

    private Integer page = 0;
    private Integer size = 10;

}

