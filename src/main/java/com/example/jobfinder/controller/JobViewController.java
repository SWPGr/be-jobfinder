package com.example.jobfinder.controller;

import com.example.jobfinder.dto.JobViewRequest;
import com.example.jobfinder.dto.job.JobViewResponse;
import com.example.jobfinder.model.JobView;
import com.example.jobfinder.service.JobViewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/job-views")
public class JobViewController {
    private final JobViewService jobViewService;

    public JobViewController(JobViewService jobViewService) {
        this.jobViewService = jobViewService;
    }

    @PostMapping
    public ResponseEntity<JobViewResponse> recordJobView(@Valid @RequestBody JobViewRequest request) {
        JobViewResponse jobView = jobViewService.recordJobView(request);
        return ResponseEntity.ok(jobView);
    }
}
