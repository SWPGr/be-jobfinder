package com.example.jobfinder.dto.application;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationSummaryAndAnalysisResponse {
    Long applicationId;
    String jobTitle;
    String seekerFullName;
    String companyName;
    String resumeSummaryContent;
    LocalDateTime resumeSummaryTimestamp;
    String jobMatchAnalysis;
    Double jobMatchScore;
    String companyFitAnalysis;
    Double companyFitScore;
    String aiAnalysisSummary;
    String aiAnalysisVersion;
    LocalDateTime analysisTimestamp;
    String rawAiResponse;
}