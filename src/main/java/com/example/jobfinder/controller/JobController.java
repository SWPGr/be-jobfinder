package com.example.jobfinder.controller;
import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.PageResponse;
import com.example.jobfinder.dto.job.*;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.JobView;
import com.example.jobfinder.service.JobService;
import com.example.jobfinder.service.JobViewService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JobController {

    JobService jobService;
    JobViewService jobViewService;

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
    public ResponseEntity<Page<JobResponse>> getAllJobs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<JobResponse> jobs = jobService.getAllJobs(pageable);

        return ResponseEntity.ok(jobs);
    }


    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long jobId) { // Kiểu dữ liệu của ID là Long
        jobViewService.recordJobView(new JobViewRequest(jobId));
        JobResponse jobResponse = jobService.getJobById(jobId);
        return ResponseEntity.ok(jobResponse);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<JobResponse>> getLatestJobs() {
        List<JobResponse> jobs = jobService.getLatestJob(10);
        return ResponseEntity.ok(jobs);
    }

    @PutMapping("/{jobId}")
    public JobResponse updateJob(@PathVariable Long jobId, @RequestBody JobUpdateRequest request) {
        return jobService.updateJob(jobId, request);
    }

    @GetMapping("/my-employer-jobs")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<PageResponse<JobResponse>>> getAllJobsForCurrentEmployer(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String jobTitle, //
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate
    ) {
        PageResponse<JobResponse> response = jobService.getAllJobsForCurrentEmployer(page, size, sortBy, sortDir, isActive, jobTitle, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.<PageResponse<JobResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Jobs for current employer fetched successfully with pagination.")
                .result(response)
                .build());
    }

    @PutMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateJobStatus(@RequestBody @Valid JobStatusUpdateRequest request) {
        try {
            jobService.updateJobStatus(request);
            String message = request.getIsActive() ? "Job activated successfully." : "Job deactivated successfully.";
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(HttpStatus.OK.value())
                    .message(message)
                    .build());
        } catch (AppException e) {
            return ResponseEntity.status(e.getErrorCode().getErrorCode())
                    .body(ApiResponse.<Void>builder()
                            .code(e.getErrorCode().getErrorCode())
                            .message(e.getErrorCode().getErrorMessage())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to update job status: " + e.getMessage())
                            .build());
        }
    }
}
