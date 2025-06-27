package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.statistic_employer.EmployerJobApplicationStatsResponse;
import com.example.jobfinder.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Để bảo vệ API
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics/employer")
@RequiredArgsConstructor
public class EmployerAnalyticsController {

    private final ApplicationService jobApplicationService;

    @GetMapping("/job-application-counts")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<EmployerJobApplicationStatsResponse>> getJobApplicationCountsForCurrentEmployer() {
        EmployerJobApplicationStatsResponse response = jobApplicationService.getApplicationsPerJobForCurrentEmployer();

        return ResponseEntity.ok(ApiResponse.<EmployerJobApplicationStatsResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Job application counts for current employer fetched successfully")
                .result(response)
                .build());
    }
}