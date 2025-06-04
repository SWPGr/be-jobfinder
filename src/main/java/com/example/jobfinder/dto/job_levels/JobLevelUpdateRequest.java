package com.example.jobfinder.dto.job_levels;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobLevelUpdateRequest {
    @NotBlank(message = "Job level name cannot be blank")
    @Size(min = 2, max = 100, message = "Job level name must be between 2 and 100 characters")
    private String name;
}