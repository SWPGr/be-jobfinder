package com.example.jobfinder.controller;

import com.example.jobfinder.dto.job.SavedJobRequest;
import com.example.jobfinder.dto.job.SavedJobResponse;
import com.example.jobfinder.model.SavedJob;
import com.example.jobfinder.service.SavedJobService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SavedJobController {

    private final SavedJobService savedJobService;

    public SavedJobController(SavedJobService savedJobService) {
        this.savedJobService = savedJobService;
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
