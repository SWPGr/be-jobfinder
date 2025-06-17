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

    /****
     * Constructs a new SavedJobController with the specified SavedJobService.
     */
    public SavedJobController(SavedJobService savedJobService) {
        this.savedJobService = savedJobService;
    }

    /**
     * Saves a job for a user based on the provided request data.
     *
     * @param request the details of the job to be saved
     * @return a response entity containing the saved job information
     */
    @PostMapping("/saved-jobs")
    public ResponseEntity<SavedJobResponse> saveJob(@RequestBody SavedJobRequest request) {
        SavedJobResponse savedJob = savedJobService.savedJob(request);
        return ResponseEntity.ok(savedJob);
    }
    /**
     * Removes a saved job for a user based on the provided request data.
     *
     * @param request the request containing information needed to identify and remove the saved job
     * @return an HTTP 200 OK response with no content
     */
    @DeleteMapping("/saved-jobs/{jobId}")
    public ResponseEntity<Void> unSaveJob(@Valid @RequestBody SavedJobRequest request) {
        savedJobService.unSaveJob(request);
        return ResponseEntity.ok().build();
    }
}
