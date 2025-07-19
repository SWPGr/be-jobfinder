package com.example.jobfinder.dto.green;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyGreenMetrics {
    private List<String> marketKeywords = new ArrayList<>();
    private List<String> marketCategories = new ArrayList<>();
    private double marketPositioningScore = 0.0;

    private List<String> csrKeywords = new ArrayList<>();
    private List<String> csrCategories = new ArrayList<>();
    private double csrSustainabilityScore = 0.0;

    private List<String> profileKeywords = new ArrayList<>();
    private List<String> additionalKeywords = new ArrayList<>();

    private boolean hasGreenCompanyName = false;
    private boolean hasGreenWebsite = false;

    public List<String> getAllKeywords() {
        List<String> all = new ArrayList<>(marketKeywords);
        all.addAll(csrKeywords);
        all.addAll(profileKeywords);
        all.addAll(additionalKeywords);
        return all.stream().distinct().collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        List<String> all = new ArrayList<>(marketCategories);
        all.addAll(csrCategories);
        return all.stream().distinct().collect(Collectors.toList());
    }
}