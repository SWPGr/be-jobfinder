package com.example.jobfinder.controller;

import com.example.jobfinder.dto.JobRecommendationResponse;
import com.example.jobfinder.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public ResponseEntity<Void> generateRecommendations() {
        recommendationService.generateRecommendations();
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<JobRecommendationResponse>> getRecommendations() {
        List<JobRecommendationResponse> recommendations = recommendationService.getRecommendations();
        return ResponseEntity.ok(recommendations);
    }
}
