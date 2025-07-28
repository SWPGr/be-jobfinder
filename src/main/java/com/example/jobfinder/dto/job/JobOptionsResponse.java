package com.example.jobfinder.dto.job;

import com.example.jobfinder.model.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class JobOptionsResponse {
    List<JobType> jobTypes;
    List<JobLevel> jobLevels;
    List<Education> educations;
    List<Category> categories;
    List<Experience> experiences;
    List<Organization> organizations;

    public JobOptionsResponse(List<JobType> jobTypes, List<JobLevel> jobLevels,
                              List<Education> educations, List<Category> categories,
                              List<Experience> experiences, List<Organization> organizations) {
        this.jobTypes = jobTypes;
        this.jobLevels = jobLevels;
        this.educations = educations;
        this.categories = categories;
        this.experiences = experiences;
        this.organizations = organizations;
    }

    // Getters (nếu cần)
}

