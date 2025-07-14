package com.example.jobfinder.dto.statistic_job_seeker;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobSeekerDashboardResponse {
    long totalAppliedJobs;
    long totalSavedJobs;
    long totalJobRecommendations;
}
