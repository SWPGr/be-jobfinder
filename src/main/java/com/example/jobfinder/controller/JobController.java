package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.JobCreationRequest;
import com.example.jobfinder.dto.JobResponse;
import com.example.jobfinder.dto.JobUpdateRequest;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.service.JobService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobController {

    JobService jobService; // Spring sẽ inject JobService vào đây

    @PostMapping
    public ApiResponse<Job> createJob(@RequestBody @Valid JobCreationRequest request) {
        ApiResponse<Job> response = new ApiResponse<>();
        response.setResult(jobService.createJob(request));
        return response;
    }

    @GetMapping
    public List<JobResponse> getAllJobs() {
        return jobService.getAllJobs();
    }

    @GetMapping("/{jobId}")
    public JobResponse getJobById(@PathVariable Long jobId) { // Kiểu dữ liệu của ID là Long
        return jobService.getJobById(jobId);
    }

    @PutMapping("/{jobId}")
    public JobResponse updateJob(@PathVariable Long jobId, @RequestBody JobUpdateRequest request) { // ID là Long

        return jobService.updateJob(jobId, request);
    }

    @DeleteMapping("/{jobId}")
    public String deleteJob(@PathVariable Long jobId) { // ID là Long
        jobService.deleteJob(jobId);
        return "Job with ID " + jobId + " has been deleted successfully!";
    }
}
