package com.example.jobfinder.dto.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiIntentResponse {
    private List<Candidate> candidates;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Candidate {
        private Content content;
        // Các trường khác như finishReason, safetyRatings có thể bỏ qua nếu không cần
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String text;
    }

    // DTO để parse JSON Intent mà Gemini trả về
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntentAnalysisResult {
        private String intent; // Ví dụ: "job_search", "user_info", "subscription_info", "general_chat", "unclear"
        @JsonProperty("jobSearchParams")
        private JobSearchParams jobSearchParams;
        @JsonProperty("userSearchParams") // Tham số cho tìm kiếm người dùng
        private UserSearchParams userSearchParams;
        @JsonProperty("subscriptionSearchParams") // Tham số cho tìm kiếm gói đăng ký
        private SubscriptionSearchParams subscriptionSearchParams;
        @JsonProperty("companyInfoParams") // Tham số cho tìm kiếm thông tin công ty (từ user_details)
        private CompanyInfoParams companyInfoParams;
        @JsonProperty("employerReviewParams") // Tham số cho tìm kiếm đánh giá nhà tuyển dụng
        private EmployerReviewParams employerReviewParams;
        @JsonProperty("applicationSearchParams") // Tham số cho tìm kiếm đơn ứng tuyển
        private ApplicationSearchParams applicationSearchParams;
        // Thêm các loại tham số khác tùy theo database
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JobSearchParams {
        @JsonProperty("job_title")
        private String jobTitle;
        private String location;
        @JsonProperty("min_salary")
        private Float minSalary; // Đổi thành Float vì salary_min/max là FLOAT
        @JsonProperty("max_salary")
        private Float maxSalary; // Đổi thành Float
        private String category;
        @JsonProperty("job_level")
        private String jobLevel;
        @JsonProperty("job_type")
        private String jobType;
        @JsonProperty("employer_name")
        private String employerName; // Để tìm jobs theo tên công ty
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSearchParams {
        private String email;
        @JsonProperty("full_name")
        private String fullName;
        private String role; // Role name, ví dụ: JOB_SEEKER, EMPLOYER
        private String location;
        @JsonProperty("years_experience")
        private Integer yearsExperience;
        @JsonProperty("is_premium")
        private Boolean isPremium;
        @JsonProperty("is_verified")
        private Integer isVerified;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionSearchParams {
        private String userEmail;
        @JsonProperty("plan_name")
        private String planName;
        @JsonProperty("is_active")
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyInfoParams {
        @JsonProperty("company_name")
        private String companyName;
        private String location;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployerReviewParams {
        @JsonProperty("employer_name")
        private String employerName;
        @JsonProperty("min_rating")
        private Integer minRating;
        @JsonProperty("max_rating")
        private Integer maxRating;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationSearchParams {
        @JsonProperty("job_title")
        private String jobTitle;
        @JsonProperty("job_seeker_email")
        private String jobSeekerEmail;
        private String status; // Pending, Accepted, Rejected
    }
}