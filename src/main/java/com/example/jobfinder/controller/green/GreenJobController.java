package com.example.jobfinder.controller.green;

import com.example.jobfinder.dto.job.JobRecommendationResponse;
import com.example.jobfinder.service.GreenJobDetectionService;
import com.example.jobfinder.dto.green.GreenJobAnalysis;
import com.example.jobfinder.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/green-jobs")
@RequiredArgsConstructor
public class GreenJobController {

    private final GreenJobDetectionService greenJobDetectionService;
    private final RecommendationService recommendationService;

    @PostMapping("/analyze")
    public ResponseEntity<GreenJobAnalysis> analyzeGreenJob(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String description = request.get("description");
        String company = request.get("company");

        GreenJobAnalysis analysis = greenJobDetectionService.analyzeGreenJob(title, description, company);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<JobRecommendationResponse>> getGreenJobRecommendations() {
        List<JobRecommendationResponse> recommendations = recommendationService.getGreenJobRecommendations();
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/generate-recommendations")
    public ResponseEntity<Void> generateGreenJobRecommendations() {
        recommendationService.generateGreenJobRecommendations();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getGreenJobSuggestions(@RequestParam String userProfile) {
        List<String> suggestions = greenJobDetectionService.getGreenJobSuggestions(userProfile);
        return ResponseEntity.ok(suggestions);
    }
}