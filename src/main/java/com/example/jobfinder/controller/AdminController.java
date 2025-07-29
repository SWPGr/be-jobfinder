package com.example.jobfinder.controller;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final JobService jobService;

    @GetMapping("/list-job")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<JobResponse>> getAllJobs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<JobResponse> jobs = jobService.getAllJobsForAdmin(pageable);

        return ResponseEntity.ok(jobs);
    }
}