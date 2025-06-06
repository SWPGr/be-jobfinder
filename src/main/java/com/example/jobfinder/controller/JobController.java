package com.example.jobfinder.controller;

import com.example.jobfinder.dto.JobRequest;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.service.ApplicationService;
import com.example.jobfinder.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }


    @PostMapping
    public ResponseEntity<Job> createJob(@RequestBody JobRequest request) {
        Job job = jobService.createJob(request);
        return ResponseEntity.ok(job);
    }

}
