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

    /**
     * Constructs a new RecommendationController with the specified RecommendationService.
     */
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Triggers the generation of job recommendations.
     *
     * Initiates the recommendation generation process and returns an HTTP 200 OK response upon completion.
     * No response body is included.
     *
     * @return HTTP 200 OK if the recommendation generation is triggered successfully
     */
    @PostMapping
    public ResponseEntity<Void> generateRecommendations() {
        recommendationService.generateRecommendations();
        return ResponseEntity.ok().build();
    }

    /**
     * Handles HTTP GET requests to retrieve job recommendations.
     *
     * @return a ResponseEntity containing a list of job recommendations with HTTP status 200 OK
     */
    @GetMapping
    public ResponseEntity<List<JobRecommendationResponse>> getRecommendations() {
        List<JobRecommendationResponse> recommendations = recommendationService.getRecommendations();
        return ResponseEntity.ok(recommendations);
    }
}
