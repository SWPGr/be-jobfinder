package com.example.jobfinder.dto.job;

import com.example.jobfinder.model.*;

import java.util.List;

public record JobOptionsResponse(
    List<JobType> jobTypes,
    List<JobLevel> jobLevels,
    List<Education> educations,
    List<Category> categories,
    List<Experience> experiences,
    List<Organization> organizations
    ) {}
