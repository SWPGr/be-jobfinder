package com.example.jobfinder.dto.job;

import com.example.jobfinder.model.*;
import com.example.jobfinder.model.enums.OrganizationType;

import java.util.List;

public record JobOptionsResponse(
    List<JobType> jobTypes,
    List<JobLevel> jobLevels,
    List<Education> educations,
    List<Category> categories,
    List<Experience> experiences,
    List<OrganizationType> organizations
    ) {}
