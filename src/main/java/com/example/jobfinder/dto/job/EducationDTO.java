package com.example.jobfinder.dto.job;

import com.example.jobfinder.model.Education;

public record EducationDTO(Long id, String name) {
    public static EducationDTO fromEntity(Education education) {
        return new EducationDTO(education.getId(), education.getName());
    }
}