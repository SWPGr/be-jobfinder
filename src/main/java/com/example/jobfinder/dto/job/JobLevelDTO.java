package com.example.jobfinder.dto.job;

import com.example.jobfinder.model.JobLevel;

public record JobLevelDTO(Long id, String name) {
    public static JobLevelDTO fromEntity(JobLevel jobLevel) {
        return new JobLevelDTO(jobLevel.getId(), jobLevel.getName());
    }
}