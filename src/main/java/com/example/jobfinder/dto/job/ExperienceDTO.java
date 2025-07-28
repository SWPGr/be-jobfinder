package com.example.jobfinder.dto.job;

import com.example.jobfinder.model.Experience;

public record ExperienceDTO(Long id, String name) {
    public static ExperienceDTO fromEntity(Experience experience) {
        return new ExperienceDTO(experience.getId(), experience.getName());
    }
}