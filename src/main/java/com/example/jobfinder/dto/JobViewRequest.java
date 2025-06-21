package com.example.jobfinder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobViewRequest {
    @NotNull(message = "Job ID is required")
    private Long jobId;

}
