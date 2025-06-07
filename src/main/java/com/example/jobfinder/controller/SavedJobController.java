package com.example.jobfinder.controller;

import com.example.jobfinder.dto.SavedJobRequest;
import com.example.jobfinder.model.SavedJob;
import com.example.jobfinder.service.SavedJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SavedJobController {

    private final SavedJobService savedJobService;

    public SavedJobController(SavedJobService savedJobService) {
        this.savedJobService = savedJobService;
    }

    @PostMapping("/save")
    public ResponseEntity<SavedJob> saveJob(@RequestBody SavedJobRequest request) {
        SavedJob savedJob = savedJobService.savedJob(request);
        return ResponseEntity.ok(savedJob);
    }
}
