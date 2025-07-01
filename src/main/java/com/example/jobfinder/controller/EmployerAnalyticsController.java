package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.PageResponse;
import com.example.jobfinder.dto.application.ApplicationResponse;
import com.example.jobfinder.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics/employer")
@RequiredArgsConstructor
public class EmployerAnalyticsController {

    private final ApplicationService jobApplicationService;

    @GetMapping("/job-applications")
    @PreAuthorize("hasRole('Employer')")
    public ApiResponse<PageResponse<ApplicationResponse>> getEmployerJobApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minYearsExperience,
            @RequestParam(required = false) Long educationId
    ) {
        PageResponse<ApplicationResponse> response = jobApplicationService.getEmployerJobApplications(
                page, size, sortBy, sortDir, fullName, email, location, minYearsExperience, educationId);

        return ApiResponse.<PageResponse<ApplicationResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Job applications retrieved successfully")
                .result(response)
                .build();
    }

}