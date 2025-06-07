package com.example.jobfinder.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class JobRequest {
    private Long employerId;
    private String title;
    private String description;
    private String location;
    private Float salaryMin;
    private Float salaryMax;
    private Long categoryId;
    private Long jobLevelId;
    private Long jobTypeId;
}
