package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApplicationRequest;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.model.Application;
import com.example.jobfinder.service.ApplicationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/apply")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationController {

   ApplicationService applicationService;


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
}
