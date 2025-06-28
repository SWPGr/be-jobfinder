package com.example.jobfinder.dto.job;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobRecommendationResponse {
    private Long jobId;
    private String title;
    private String location;
    private String category;
    private String jobLevel;
    private String jobType;
    private float score;
}
