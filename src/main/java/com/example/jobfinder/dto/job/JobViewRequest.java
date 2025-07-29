package com.example.jobfinder.dto.job;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobViewRequest {
    @NotNull(message = "Job ID is required")
    private Long jobId;

}
