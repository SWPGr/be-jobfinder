package com.example.jobfinder.controller;

import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.job.SavedJobRequest;
import com.example.jobfinder.dto.job.SavedJobResponse;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.UserRepository;
import com.example.jobfinder.service.SavedJobService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.jobfinder.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SavedJobController {
    final SavedJobService savedJobService;
    final UserRepository userRepository;

    @GetMapping("/saved-jobs")
    public ResponseEntity<Page<JobResponse>> getSavedJobsForCurrentUser(Pageable pageable) { // <-- Thêm Pageable
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHENTICATED.getErrorMessage());
        }

        String userEmail = authentication.getName();

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorMessage()));

        // Thay đổi kiểu trả về từ List<JobResponse> sang Page<JobResponse>
        Page<JobResponse> savedJobsPage = savedJobService.getSavedJobsByJobSeekerId(currentUser.getId(), pageable);
        return ResponseEntity.ok(savedJobsPage);
    }

    @PostMapping("/saved-jobs")
    public ResponseEntity<SavedJobResponse> saveJob(@RequestBody SavedJobRequest request) {
        SavedJobResponse savedJob = savedJobService.savedJob(request);
        return ResponseEntity.ok(savedJob);
    }
    @DeleteMapping("/saved-jobs/{jobId}")
    public ResponseEntity<Void> unSaveJob(@Valid @PathVariable Long jobId) {
        savedJobService.unSaveJob(jobId);
        return ResponseEntity.ok().build();
    }
}