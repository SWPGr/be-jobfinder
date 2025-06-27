package com.example.jobfinder.dto.statistic_admin;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyApplicationStatsResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DailyApplicationCountResponse> dailyCounts;
    private Long totalApplications; // Tổng số trong cả kỳ
}