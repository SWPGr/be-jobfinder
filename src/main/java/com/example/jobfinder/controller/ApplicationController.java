package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.application.ApplicationRequest;
import com.example.jobfinder.dto.application.ApplicationResponse;
import com.example.jobfinder.dto.application.ApplicationStatusUpdateRequest;
import com.example.jobfinder.dto.application.CandidateFilterRequest;
import com.example.jobfinder.dto.job.CandidateDetailResponse;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.statistic_admin.DailyApplicationCountResponse;
import com.example.jobfinder.dto.statistic_admin.MonthlyApplicationStatsResponse;
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.Application;
import com.example.jobfinder.model.User;
import com.example.jobfinder.model.enums.ApplicationStatus;
import com.example.jobfinder.repository.ApplicationRepository;
import com.example.jobfinder.repository.UserRepository;
import com.example.jobfinder.service.ApplicationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;


import java.time.LocalDate;
import java.util.stream.Collectors;

import static java.lang.Math.log;

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
    public ResponseEntity<Page<ApplicationResponse>> getMyApplications(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHENTICATED.getErrorMessage());
        }
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorMessage()));
        if (!currentUser.getRole().getName().equals("JOB_SEEKER") && !currentUser.getRole().getName().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ErrorCode.UNAUTHORIZED.getErrorMessage());
        }
        Page<ApplicationResponse> myApplicationsPage = applicationService.getApplicationsByJobSeekerId(currentUser.getId(), pageable);
        return ResponseEntity.ok(myApplicationsPage);
    }

    @GetMapping("/candidates/{jobId}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<CandidateDetailResponse>>> getCandidatesForEmployerJob(
            @PathVariable Long jobId,
            @ModelAttribute CandidateFilterRequest filterRequest,
            Pageable pageable) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHENTICATED.getErrorMessage());
        }

        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorMessage()));
        if (currentUser.getRole().getName().equals("EMPLOYER")) {
            boolean isEmployerJob = applicationService.isJobOwnedByEmployer(jobId, currentUser.getId());
            if (!isEmployerJob) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, ErrorCode.UNAUTHORIZED.getErrorMessage());
            }
        }
        Page<CandidateDetailResponse> candidatesPage = applicationService.getCandidatesDetailByJobId(jobId, filterRequest, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<CandidateDetailResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Candidates for job " + jobId + " fetched successfully with filters.")
                .result(candidatesPage)
                .build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApplicationResponse> applyJob(@ModelAttribute ApplicationRequest request) throws Exception {
        return ResponseEntity.ok(applicationService.applyJob(request));

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
        ApplicationController.log.info("API: Lấy tổng số lượng ứng tuyển công việc.");
        long totalApplications = applicationService.getTotalApplications();
        return ApiResponse.<Long>builder()
                .code(HttpStatus.OK.value())
                .message("Total applications count fetched successfully")
                .result(totalApplications)
                .build();
    }

    @GetMapping("/{applicationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationDetail(
            @PathVariable Long applicationId,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHENTICATED.getErrorMessage());
        }

        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorMessage()));

        Long currentUserId = currentUser.getId();
        String currentUserRole = currentUser.getRole().getName();

        try {
            ApplicationResponse applicationDetail = applicationService.getApplicationDetails(
                    applicationId, currentUserId, currentUserRole);

            return ResponseEntity.ok(ApiResponse.<ApplicationResponse>builder()
                    .code(HttpStatus.OK.value())
                    .message("Application details fetched successfully.")
                    .result(applicationDetail)
                    .build());
        } catch (AppException e) {
            log.error("Error fetching application details for ID {}: {}", applicationId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.valueOf(e.getErrorCode().getErrorCode()), e.getErrorCode().getErrorMessage(), e);
        } catch (Exception e) {
            log.error("An unexpected error occurred while fetching application details for ID {}: {}", applicationId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{applicationId}/summarize-resume")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')") // Chỉ EMPLOYER hoặc ADMIN được tóm tắt resume
    public ResponseEntity<ApiResponse<String>> summarizeResume(
            @PathVariable Long applicationId,
            Authentication authentication) {

        // Kiểm tra xác thực và lấy thông tin người dùng (tương tự các API khác của bạn)
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHENTICATED.getErrorMessage());
        }

        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorMessage()));
        if (currentUser.getRole().getName().equals("EMPLOYER")) {
            log.warn("Lưu ý: Đối với EMPLOYER, bạn nên thêm kiểm tra quyền sở hữu job cho application ID {}.", applicationId);
        }

        if (currentUser.getRole().getName().equals("EMPLOYER")) {
            boolean isEmployerJob = applicationService.isJobOwnedByEmployerByApplicationId(applicationId, currentUser.getId());
            if (!isEmployerJob) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, ErrorCode.UNAUTHORIZED.getErrorMessage());
            }
        }

        try {
            String resumeSummary = applicationService.summarizeResumeWithGemini(applicationId);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .code(HttpStatus.OK.value())
                    .message("Resume summarized successfully.")
                    .result(resumeSummary)
                    .build());
        } catch (AppException e) {
            log.error("Application error summarizing resume for ID {}: {}", applicationId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.valueOf(e.getErrorCode().getErrorCode()), e.getErrorCode().getErrorMessage(), e);
        } catch (IOException e) {
            log.error("IO error when calling Gemini API for resume summary (ID {}): {}", applicationId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi giao tiếp với AI để tóm tắt resume.", e);
        } catch (Exception e) {
            log.error("Unexpected error summarizing resume for ID {}: {}", applicationId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi không xác định khi tóm tắt resume.", e);
        }
    }

    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<String>>> getAllApplicationStatuses() {
        List<String> statuses = Arrays.stream(ApplicationStatus.values())
                .map(ApplicationStatus::getValue) // Hoặc .name() nếu bạn muốn tên enum
                .collect(Collectors.toList());

        ApiResponse<List<String>> apiResponse = ApiResponse.<List<String>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved all application statuses")
                .result(statuses)
                .build();

        return ResponseEntity.ok(apiResponse);
    }



    private Long getUserIdFromAuthentication(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
