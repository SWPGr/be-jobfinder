// src/main/java/com/example/jobfinder/dto/response/DailyTrendResponse.java
package com.example.jobfinder.dto.statistic_admin;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyTrendResponse {
    private String date; // Ví dụ: "2023-01-15"
    private long totalJobSeekers;
    private long totalEmployers;
    private long totalActiveJobs;
    private long totalAppliedJobs;
    private long totalJobs;
}