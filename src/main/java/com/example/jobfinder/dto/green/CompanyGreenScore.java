package com.example.jobfinder.dto.green;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyGreenScore {
    private double marketPositioningScore;
    private double csrSustainabilityScore;
    private double profileBonus;
    private double industryMultiplier;
    private double overallScore;
}