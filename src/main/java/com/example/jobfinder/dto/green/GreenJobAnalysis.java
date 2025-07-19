package com.example.jobfinder.dto.green;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GreenJobAnalysis {
    private boolean isGreenJob;
    private double greenScore;
    private List<String> detectedKeywords;
    private List<String> greenCategories;
    private String primaryGreenCategory;
    private List<String> suggestions;

    public GreenJobAnalysis(boolean isGreenJob, double greenScore,
                            List<String> detectedKeywords, List<String> greenCategories) {
        this.isGreenJob = isGreenJob;
        this.greenScore = greenScore;
        this.detectedKeywords = detectedKeywords;
        this.greenCategories = greenCategories;
        this.primaryGreenCategory = greenCategories.isEmpty() ? null : greenCategories.get(0);
    }

}
