package com.example.jobfinder.dto.green;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GreenCompanyAnalysis {
    private Long UserDetailId;
    private boolean isGreenCompany;
    private double overallGreenScore;

    // Detailed scores
    private double marketPositioningScore;
    private double csrSustainabilityScore;

    // Analysis results
    private List<String> greenCategories;
    private List<String> greenKeywords;
    private String primaryGreenCategory;

    // Assessment details
    private List<String> greenStrengths;
    private List<String> improvementAreas;
    private List<String> recommendations;

    // Benchmarking
    private double industryBenchmark;
    private String certificationLevel;

    // Additional metadata
    private String analysisDate;
    private String analysisVersion;
}