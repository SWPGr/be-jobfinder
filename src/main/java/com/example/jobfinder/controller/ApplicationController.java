package com.example.jobfinder.controller;

import com.example.jobfinder.dto.application.ApplicationRequest;
import com.example.jobfinder.dto.application.ApplicationResponse;
import com.example.jobfinder.dto.application.ApplicationStatusUpdateRequest;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.Application;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.UserRepository;
import com.example.jobfinder.service.ApplicationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/apply")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationController {

    ApplicationService applicationService;
    UserRepository userRepository;


    @GetMapping("/user/{userId}/jobs")
    public ResponseEntity<List<JobResponse>> getAppliedJobsForUser(@PathVariable Long userId) {
        List<JobResponse> appliedJobs = applicationService.getAppliedJobsByUserId(userId);
        return ResponseEntity.ok(appliedJobs);
    }

    @GetMapping("/candidates/{jobId}")
    public ResponseEntity<List<UserResponse>> getCandidatesByJob(@PathVariable Long jobId,
                                                                 Authentication authentication) {
        List<UserResponse> candidates = applicationService.getCandidatesByJobId(jobId);
        return ResponseEntity.ok(candidates);
    }

    @PostMapping
    public ResponseEntity<Application> applyJob(@RequestBody ApplicationRequest request) throws Exception {
        Application application = applicationService.applyJob(request);
        return ResponseEntity.ok(application);
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

    private Long getUserIdFromAuthentication(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
