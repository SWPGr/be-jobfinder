package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.application.ApplicationRequest;
import com.example.jobfinder.dto.application.ApplicationResponse;
import com.example.jobfinder.dto.application.ApplicationStatusUpdateRequest;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.Application;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.ApplicationRepository;
import com.example.jobfinder.repository.UserRepository;
import com.example.jobfinder.service.ApplicationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;

@RestController
@RequestMapping("api/apply")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationController {

    ApplicationService applicationService;
    UserRepository userRepository;
    ApplicationRepository applicationRepository;

    @GetMapping("/my-applied-jobs")
    public ResponseEntity<List<JobResponse>> getAppliedJobsForUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHENTICATED.getErrorMessage());
        }

        String userEmail = authentication.getName();
        com.example.jobfinder.model.User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorMessage()));

        if (!currentUser.getRole().getName().equals("JOB_SEEKER") && !currentUser.getRole().getName().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ErrorCode.UNAUTHORIZED.getErrorMessage());
        }

        List<JobResponse> appliedJobs = applicationService.getAppliedJobsByUserId(currentUser.getId());
        return ResponseEntity.ok(appliedJobs);
    }

    @GetMapping("/{jobId}/candidates") // Endpoint /api/applications/{jobId}/candidates
    public ResponseEntity<List<UserResponse>> getCandidatesByJob(@PathVariable Long jobId) { // Loại bỏ Authentication khỏi param vì đã lấy từ SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHENTICATED.getErrorMessage());
        }

        String userEmail = authentication.getName();
        com.example.jobfinder.model.User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorMessage()));

        // Logic phân quyền: Chỉ EMPLOYER (chủ sở hữu job) hoặc ADMIN mới được xem
        if (currentUser.getRole().getName().equals("EMPLOYER") ||  currentUser.getRole().getName().equals("ADMIN")) {
            // Kiểm tra xem công việc này có phải do EMPLOYER hiện tại đăng không
            boolean isEmployerJob = applicationService.isJobOwnedByEmployer(jobId, currentUser.getId());
            if (!isEmployerJob) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, ErrorCode.UNAUTHORIZED.getErrorMessage());
            }
        } else {
            // Nếu không phải EMPLOYER và cũng không phải ADMIN, từ chối
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ErrorCode.UNAUTHORIZED.getErrorMessage());
        }

        List<UserResponse> candidates = applicationService.getCandidatesByJobId(jobId);
        return ResponseEntity.ok(candidates);
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> applyJob(@RequestBody ApplicationRequest request) throws Exception {
        ApplicationResponse applicationResponse  = applicationService.applyJob(request);
        return new ResponseEntity<>(applicationResponse, HttpStatus.CREATED);

    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody ApplicationStatusUpdateRequest request,
            Authentication authentication) {

        Long employerId = getUserIdFromAuthentication(authentication); // Lấy ID của nhà tuyển dụng đang đăng nhập

        ApplicationResponse updatedApplication = applicationService.updateApplicationStatus(
                applicationId, request, employerId);
        return ResponseEntity.ok(updatedApplication);
    }

    @GetMapping("/total")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Long> getTotalApplications() {
        log.info("API: Lấy tổng số lượng ứng tuyển công việc.");
        long totalApplications = applicationService.getTotalApplications();
        return ApiResponse.<Long>builder()
                .code(HttpStatus.OK.value())
                .message("Total applications count fetched successfully")
                .result(totalApplications)
                .build();
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
