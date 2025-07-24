package com.example.jobfinder.dto.job;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobStatusUpdateRequest {
    @NotNull(message = "Job ID cannot be null")
    private Long jobId;

    @NotNull(message = "Active status cannot be null")
    private Boolean isActive;
}