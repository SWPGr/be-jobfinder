package com.example.jobfinder.dto.job;

import com.example.jobfinder.model.JobType;

public record JobTypeDTO(Long id, String name) {
    public static JobTypeDTO fromEntity(JobType jobType) {
        return new JobTypeDTO(jobType.getId(), jobType.getName());
    }
}
