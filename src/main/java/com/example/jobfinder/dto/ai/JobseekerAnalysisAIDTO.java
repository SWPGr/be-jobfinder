package com.example.jobfinder.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List; // Để lưu danh sách các kỹ năng, dự án, v.v. nếu cần chi tiết hơn

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobseekerAnalysisAIDTO {
    // Thông tin cơ bản
    private String fullName;
    private String email;
    private String phoneNumber;
    private String careerObjectiveSummary;

    // Tóm tắt các phần chính của resume
    private String workExperienceSummary; // Có thể giữ string tổng hợp
    // Hoặc chi tiết hơn nếu cần: private List<ExperienceEntry> workExperiences;
    // (trong đó ExperienceEntry là một nested class/DTO nhỏ)

    private String skillsSummary; // Có thể giữ string tổng hợp
    // Hoặc chi tiết hơn: private List<SkillCategory> skills;
    // (trong đó SkillCategory có name và List<String> items)

    private String educationSummary; // string tổng hợp
    private String projectsActivitiesSummary; // string tổng hợp
    private String certificationsAwardsSummary; // string tổng hợp
    private String otherActivitiesSummary; // string tổng hợp

    // Các trường phân tích thêm từ AI (để lọc)
    private List<String> keyTechnologiesTools; // Danh sách các công nghệ, công cụ quan trọng
    private List<String> softSkillsIdentified; // Danh sách các kỹ năng mềm
    private List<String> domainExpertiseKeywords; // Danh sách các lĩnh vực chuyên môn chính
    private String careerLevelPrediction; // Dự đoán cấp độ nghề nghiệp (Fresher, Junior, Mid, Senior, Lead)
    private String potentialEventSuitability; // Đánh giá sự phù hợp chung cho các loại sự kiện
    private List<String> relevantEventKeywords; // Các từ khóa liên quan đến sự kiện tiềm năng

    // Các nested class/DTO mẫu nếu bạn muốn cấu trúc chi tiết hơn cho một số phần
    /*
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ExperienceEntry {
        private String companyName;
        private String jobTitle;
        private String duration;
        private String responsibilitiesAndAchievements;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class SkillCategory {
        private String categoryName; // e.g., "Ngôn ngữ lập trình", "Frameworks"
        private List<String> skills;
    }
    */
}