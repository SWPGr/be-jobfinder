// src/main/java/com/example/jobfinder/dto/response/MonthlyTrendResponse.java
package com.example.jobfinder.dto.statistic_admin;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyTrendResponse {
    private String monthYear; // Ví dụ: "2023-01"
    private long totalJobSeekers;
    private long totalEmployers;
    private long totalActiveJobs;
    private long totalAppliedJobs;
    private long totalJobs;
}