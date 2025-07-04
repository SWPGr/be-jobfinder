package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.PageResponse;
import com.example.jobfinder.dto.application.ApplicationResponse;
import com.example.jobfinder.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics/employer")
@RequiredArgsConstructor
public class EmployerAnalyticsController {

    private final ApplicationService applicationService;

    @GetMapping("/{jobId}/applications")
    @PreAuthorize("hasRole('EMPLOYER')") // Ensures only users with 'EMPLOYER' role can access
    public ApiResponse<PageResponse<ApplicationResponse>> getEmployerJobApplicationsForSpecificJob(
            @PathVariable Long jobId, // Job ID from the URL path
            @RequestParam(defaultValue = "0") int page, // Page number, default 0
            @RequestParam(defaultValue = "10") int size, // Page size, default 10
            @RequestParam(defaultValue = "newest") String sortOrder, // Sort order, default "newest"

            // Applicant-related filters
            @RequestParam(required = false) String name, // Optional filter by applicant's name
            @RequestParam(required = false) Integer minExperience, // Optional filter by min experience
            @RequestParam(required = false) Integer maxExperience, // Optional filter by max experience

            // Job-related filters (applied to the job associated with the application)
            @RequestParam(required = false) Long jobTypeId, // Optional filter by job type ID
            @RequestParam(required = false) Long educationId, // Optional filter by applicant's education ID
            @RequestParam(required = false) Long jobLevelId // Optional filter by job level ID
    ) {
        // Delegate the actual business logic to the ApplicationService
        PageResponse<ApplicationResponse> response = applicationService.getEmployerJobApplicationsForSpecificJob(
                jobId, page, size, sortOrder,
                name, minExperience, maxExperience,
                jobTypeId, educationId, jobLevelId // Passing all relevant filter parameters
        );

        // Construct and return a standardized API response
        return ApiResponse.<PageResponse<ApplicationResponse>>builder()
                .code(HttpStatus.OK.value()) // HTTP status code for success
                .message("Job applications for specific job retrieved successfully") // Success message
                .result(response) // The actual paginated data
                .build();
    }

}