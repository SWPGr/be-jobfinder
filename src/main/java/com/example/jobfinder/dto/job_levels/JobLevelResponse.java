package com.example.jobfinder.dto.job_levels;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobLevelResponse {
    private Long id;
    private String name;
}