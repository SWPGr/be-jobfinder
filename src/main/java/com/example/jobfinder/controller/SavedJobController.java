package com.example.jobfinder.controller;

import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.job.SavedJobRequest;
import com.example.jobfinder.dto.job.SavedJobResponse;
import com.example.jobfinder.model.SavedJob;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.UserRepository;
import com.example.jobfinder.service.SavedJobService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication; // Import Authentication
import org.springframework.security.core.context.SecurityContextHolder; // Import SecurityContextHolder
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.jobfinder.exception.ErrorCode; // Import ErrorCode của bạn
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SavedJobController {
    final SavedJobService savedJobService;
    final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<JobResponse>> getSavedJobsForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHENTICATED.getErrorMessage());
        }

        String userEmail = authentication.getName();

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorMessage()));

        List<JobResponse> savedJobs = savedJobService.getSavedJobsByJobSeekerId(currentUser.getId());
        return ResponseEntity.ok(savedJobs);
    }

    @PostMapping("/saved-jobs")
    public ResponseEntity<SavedJobResponse> saveJob(@RequestBody SavedJobRequest request) {
        SavedJobResponse savedJob = savedJobService.savedJob(request);
        return ResponseEntity.ok(savedJob);
    }
    @DeleteMapping("/saved-jobs/{jobId}")
    public ResponseEntity<Void> unSaveJob(@Valid @RequestBody SavedJobRequest request) {
        savedJobService.unSaveJob(request);
        return ResponseEntity.ok().build();
    }
}