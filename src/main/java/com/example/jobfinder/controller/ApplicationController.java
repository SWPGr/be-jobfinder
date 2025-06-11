package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApplicationRequest;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.model.Application;
import com.example.jobfinder.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/apply")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/user/{userId}/jobs")
    public ResponseEntity<List<JobResponse>> getAppliedJobsForUser(@PathVariable Long userId) {
        List<JobResponse> appliedJobs = applicationService.getAppliedJobsByUserId(userId);
        return ResponseEntity.ok(appliedJobs);
    }


    @PostMapping
    public ResponseEntity<Application> applyJob(@RequestBody ApplicationRequest request) throws Exception {
        Application application = applicationService.applyJob(request);
        return ResponseEntity.ok(application);
    }
}
