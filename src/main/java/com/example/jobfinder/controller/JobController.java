package com.example.jobfinder.controller;
import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.PageResponse;
import com.example.jobfinder.dto.job.JobCreationRequest;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.job.JobUpdateRequest;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.service.JobService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JobController {

    JobService jobService;

    @PostMapping("/create")
    public ApiResponse<Job> createJob(@RequestBody @Valid JobCreationRequest request) {
        ApiResponse<Job> response = new ApiResponse<>();
        response.setResult(jobService.createJob(request));
        return response;
    }

    @GetMapping("/total")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới có thể xem tổng số job
    public ApiResponse<Long> getTotalJobs() {
        log.info("API: Lấy tổng số lượng công việc.");
        long totalJobs = jobService.getTotalJobs();
        return ApiResponse.<Long>builder()
                .code(HttpStatus.OK.value())
                .message("Total jobs fetched successfully")
                .result(totalJobs)
                .build();
    }

    @GetMapping("/list")
    public List<JobResponse> getAllJobs() {
        return jobService.getAllJobs();
    }
    @GetMapping("/user")
    public ResponseEntity<List<JobResponse>> getUserJobs() {
        List<JobResponse> jobs = jobService.getAllJobsForUser();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{jobId}")
    public JobResponse getJobById(@PathVariable Long jobId) { // Kiểu dữ liệu của ID là Long
        return jobService.getJobById(jobId);
    }

    @PutMapping("/{jobId}")
    public JobResponse updateJob(@PathVariable Long jobId, @RequestBody JobUpdateRequest request) {
        return jobService.updateJob(jobId, request);
    }

    @DeleteMapping("/{jobId}")
    public String deleteJob(@PathVariable Long jobId) {
        jobService.deleteJob(jobId);
        return "Job with ID " + jobId + " has been deleted successfully!";
    }

    @GetMapping("/my-employer-jobs")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<PageResponse<JobResponse>>> getAllJobsForCurrentEmployer(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PageResponse<JobResponse> response = jobService.getAllJobsForCurrentEmployer(page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.<PageResponse<JobResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Jobs for current employer fetched successfully with pagination.")
                .result(response)
                .build());
    }
}
