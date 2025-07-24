package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_detail_id", referencedColumnName = "id", unique = true, nullable = false)
    @JsonBackReference("userDetail-companyAnalysis")
    private UserDetail userDetail;

    // Các trường phân tích chung về doanh nghiệp
    @Column(name = "industry_keywords", columnDefinition = "TEXT")
    private String industryKeywords;

    @Column(name = "core_competencies", columnDefinition = "TEXT")
    private String coreCompetencies;

    @Column(name = "culture_description", columnDefinition = "TEXT")
    private String cultureDescription;

    @Column(name = "target_candidate_profile", columnDefinition = "TEXT")
    private String targetCandidateProfile;

    @Column(name = "growth_potential_summary", columnDefinition = "TEXT")
    private String growthPotentialSummary;

    @Column(name = "market_positioning_summary", columnDefinition = "TEXT")
    private String marketPositioningSummary;

    // Các trường phân tích cho mục đích hợp tác/tác động xã hội/phát triển tài năng (khái quát hóa)
    @Column(name = "csr_and_sustainability_initiatives", columnDefinition = "TEXT")
    private String csrAndSustainabilityInitiatives;

    @Column(name = "talent_development_focus", columnDefinition = "TEXT")
    private String talentDevelopmentFocus;

    @Column(name = "talent_engagement_programs", columnDefinition = "TEXT")
    private String talentEngagementPrograms;

    @Column(name = "community_outreach_programs", columnDefinition = "TEXT")
    private String communityOutreachPrograms;

    @Column(name = "event_collaboration_potential", columnDefinition = "TEXT")
    private String eventCollaborationPotential;

    @Column(name = "relevant_partnership_keywords", columnDefinition = "TEXT")
    private String relevantPartnershipKeywords;


    @Column(name = "ai_analysis_version", length = 50)
    private String aiAnalysisVersion;

    @Column(name = "analysis_timestamp")
    private LocalDateTime analysisTimestamp;

    @Column(name = "raw_ai_response", columnDefinition = "TEXT")
    private String rawAiResponse;

    // Green company analysis fields
    @Column(name = "overall_green_score")
    private Double overallGreenScore;

    @Column(name = "market_positioning_score")
    private Double marketPositioningScore;

    @Column(name = "csr_sustainability_score")
    private Double csrSustainabilityScore;

    @Column(name = "green_categories", columnDefinition = "TEXT")
    private String greenCategories;

    @Column(name = "green_keywords", columnDefinition = "TEXT")
    private String greenKeywords;

    @Column(name = "green_strengths", columnDefinition = "TEXT")
    private String greenStrengths;

    @Column(name = "improvement_areas", columnDefinition = "TEXT")
    private String improvementAreas;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "certification_level", length = 100)
    private String certificationLevel;

    @Column(name = "green_company")
    private Boolean greenCompany;

    @Column(name = "primary_green_category")
    private String primaryGreenCategory;

}