package com.example.jobfinder.dto.job;

import com.example.jobfinder.model.*;

import java.util.List;

public record JobOptionsResponse(
        List<JobTypeDTO> jobTypes,
        List<JobLevelDTO> jobLevels,
        List<EducationDTO> educations,
        List<CategoryDTO> categories,
        List<ExperienceDTO> experiences,
        List<OrganizationDTO> organizations
) {}
