package com.example.jobfinder.controller;

import com.example.jobfinder.dto.job.SavedJobRequest;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.model.SavedJob;
import com.example.jobfinder.service.SavedJobService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/save")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SavedJobController {

    final SavedJobService savedJobService;

    @GetMapping("/{jobSeekerId}")
    public ResponseEntity<List<JobResponse>> getSavedJobsForJobSeeker(@PathVariable Long jobSeekerId) { // <-- Thay đổi kiểu trả về
        List<JobResponse> savedJobs = savedJobService.getSavedJobsByJobSeekerId(jobSeekerId);
        return ResponseEntity.ok(savedJobs);
    }

    @PostMapping()
    public ResponseEntity<SavedJob> saveJob(@RequestBody SavedJobRequest request) {
        SavedJob savedJob = savedJobService.savedJob(request);
        return ResponseEntity.ok(savedJob);
    }
}
