package com.example.jobfinder.dto.job_seeker;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobSeekerSearchRequest {
    private Long educationId;
    private Long experienceId;
    private String location;
    private Integer page = 1;
    private Integer size = 10;
}
