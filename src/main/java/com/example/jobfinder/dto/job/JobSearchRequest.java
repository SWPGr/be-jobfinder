package com.example.jobfinder.dto.job;

import lombok.Data;

@Data
public class JobSearchRequest {
    private String keyword;
    private String location;
    private String category;
    private String jobLevel;
    private String jobType;
    private String education;
}

