package com.example.jobfinder.dto.job;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

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

    private Boolean isGreenJob;
    private Double greenScore;
    private List<String> greenCategories;
    private List<String> greenKeywords;
    private String primaryGreenCategory;
    private String greenJobBadge;
}
