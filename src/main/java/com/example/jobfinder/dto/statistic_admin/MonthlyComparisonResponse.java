// src/main/java/com/example/jobfinder/dto/response/MonthlyComparisonResponse.java
package com.example.jobfinder.dto.statistic_admin;

import lombok.*;
import lombok.Builder;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyComparisonResponse {
    private String monthYear; // Tháng hiện tại đang so sánh, ví dụ: "2023-02"

    private long currentMonthTotalJobs;
    private double jobsChangePercentage;
    private String jobsStatus; // "increase", "decrease", "no_change"

    private long currentMonthTotalAppliedJobs;
    private double appliedJobsChangePercentage;
    private String appliedJobsStatus; // "increase", "decrease", "no_change"

    private long currentMonthTotalJobSeekers;
    private double jobSeekersChangePercentage;
    private String jobSeekersStatus; // "increase", "decrease", "no_change"

    private long currentMonthTotalEmployers;
    private double employersChangePercentage;
    private String employersStatus; // "increase", "decrease", "no_change"
}