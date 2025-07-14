package com.example.jobfinder.controller.analytics;

import com.example.jobfinder.dto.statistic_job_seeker.JobSeekerDashboardResponse;
import com.example.jobfinder.dto.ApiResponse; // Giả sử bạn có ApiResponse chung
import com.example.jobfinder.service.StatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobseeker-analytics")
@RequiredArgsConstructor
@Slf4j

public class JobSeekerAnalyticsController {

    private final StatisticService jobSeekerAnalyticsService;

    @GetMapping("/dashboard-summary")
    @PreAuthorize("isAuthenticated() and hasRole('JOB_SEEKER')") // Chỉ cho phép JOB_SEEKER đã xác thực
    public ApiResponse<JobSeekerDashboardResponse> getDashboardSummary() {
        log.info("API: Lấy tóm tắt dashboard cho Job Seeker hiện tại.");
        JobSeekerDashboardResponse response = jobSeekerAnalyticsService.getDashboardSummaryForCurrentUser();
        return ApiResponse.<JobSeekerDashboardResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Job Seeker dashboard summary fetched successfully")
                .result(response)
                .build();
    }
}
