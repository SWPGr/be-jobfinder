package com.example.jobfinder.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobseeker_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobseekerAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_detail_id", referencedColumnName = "id", unique = true, nullable = false)
    private UserDetail userDetail;

    // Thông tin cơ bản từ resume
    @Column(name = "full_name")
    private String fullName; // Tên đầy đủ

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "career_objective_summary", columnDefinition = "TEXT")
    private String careerObjectiveSummary;

    // Các phần chính của resume, lưu dưới dạng TEXT để linh hoạt
    @Column(name = "work_experience_summary", columnDefinition = "TEXT")
    private String workExperienceSummary;

    @Column(name = "skills_summary", columnDefinition = "TEXT")
    private String skillsSummary;

    @Column(name = "education_summary", columnDefinition = "TEXT")
    private String educationSummary;

    @Column(name = "projects_activities_summary", columnDefinition = "TEXT")
    private String projectsActivitiesSummary;

    @Column(name = "certifications_awards_summary", columnDefinition = "TEXT")
    private String certificationsAwardsSummary;

    @Column(name = "other_activities_summary", columnDefinition = "TEXT")
    private String otherActivitiesSummary;

    // Các trường phân tích thêm từ AI
    @Column(name = "key_technologies_tools", columnDefinition = "TEXT")
    private String keyTechnologiesTools; // Ví dụ: Java, Spring Boot, ReactJS, Docker

    @Column(name = "soft_skills_identified", columnDefinition = "TEXT")
    private String softSkillsIdentified; // Ví dụ: Giao tiếp, Làm việc nhóm, Giải quyết vấn đề

    @Column(name = "domain_expertise_keywords", columnDefinition = "TEXT")
    private String domainExpertiseKeywords; // Ví dụ: Web Development, Mobile Development, AI/ML, Data Science

    @Column(name = "career_level_prediction", length = 50)
    private String careerLevelPrediction; // Ví dụ: Fresher, Junior, Mid, Senior, Lead

    @Column(name = "potential_event_suitability", columnDefinition = "TEXT")
    private String potentialEventSuitability; // Ví dụ: Phù hợp cho tuần lễ tuyển dụng sinh viên, Hackathon, Mentorship program

    @Column(name = "relevant_event_keywords", columnDefinition = "TEXT")
    private String relevantEventKeywords; // Ví dụ: Tuyển dụng lập trình viên, khởi nghiệp, AI, IoT, cơ hội thực tập

    @Column(name = "ai_analysis_version", length = 50)
    private String aiAnalysisVersion;

    @Column(name = "analysis_timestamp")
    private LocalDateTime analysisTimestamp;

    @Column(name = "raw_gemini_response", columnDefinition = "TEXT")
    private String rawGeminiResponse;
}