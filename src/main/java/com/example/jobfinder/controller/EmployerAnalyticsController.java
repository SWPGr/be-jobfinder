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
        PageResponse<ApplicationResponse> response = applicationService.getEmployerJobApplications(
                page, size, sortBy, sortDir, fullName, email, location, minYearsExperience, educationId);

        return ApiResponse.<PageResponse<ApplicationResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Job applications retrieved successfully")
                .result(response)
                .build();
    }


    @GetMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ApiResponse<PageResponse<ApplicationResponse>> getEmployerJobApplicationsForSpecificJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,

            // New filters for JobApplication
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String resumeUrl, // If you store resume URL in JobApplication

            // New filters for Applicant (User/UserDetail)
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String applicantLocation, // Changed name
            @RequestParam(required = false) Long educationId,
            @RequestParam(required = false) String phone, // New filter

            // New filters for Job
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) String jobLocation, // Changed name
            @RequestParam(required = false) Double minSalary, // Changed to Double, adjust if using Float/BigDecimal
            @RequestParam(required = false) Double maxSalary, // Changed to Double, adjust if using Float/BigDecimal
            @RequestParam(required = false) Long jobCategoryId,
            @RequestParam(required = false) Long jobLevelId,
            @RequestParam(required = false) Long jobTypeId
    ) {
        PageResponse<ApplicationResponse> response = applicationService.getEmployerJobApplicationsForSpecificJob(
                jobId, page, size, sortBy, sortDir,
                status, resumeUrl,
                fullName, email, applicantLocation,
                educationId, phone,
                jobTitle, jobLocation, minSalary, maxSalary,
                jobCategoryId, jobLevelId, jobTypeId
        );

        return ApiResponse.<PageResponse<ApplicationResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Job applications for specific job retrieved successfully")
                .result(response)
                .build();
    }
}