package com.example.jobfinder.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyAnalysisAIDTO {
    // Các trường phân tích chung về doanh nghiệp
    private String industryKeywords;
    private String coreCompetencies;
    private String cultureDescription;
    private String targetCandidateProfile;
    private String growthPotentialSummary;
    private String marketPositioningSummary;

    // Các trường phân tích cho mục đích hợp tác/tác động xã hội/phát triển tài năng (khái quát hóa)
    private String csrAndSustainabilityInitiatives;
    private String talentDevelopmentFocus;
    private String talentEngagementPrograms;
    private String communityOutreachPrograms;
    private String eventCollaborationPotential;
    private String relevantPartnershipKeywords;
}