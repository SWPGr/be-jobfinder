// src/main/java/com/example/jobfinder/service/ChatbotService.java
package com.example.jobfinder.service;

import com.example.jobfinder.dto.gemini.GeminiIntentResponse;
import com.example.jobfinder.dto.chatbot.ChatbotMessageRequest;
import com.example.jobfinder.dto.chatbot.ChatbotHistoryResponse;
import com.example.jobfinder.model.*;
import com.example.jobfinder.repository.*;
import com.example.jobfinder.exception.AppException; // Sử dụng AppException thay vì ResponseStatusException
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.ChatbotHistoryMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatbotService {

    GeminiService geminiService;
    JobRepository jobRepository;
    UserRepository userRepository;
    UserDetailsRepository userDetailsRepository;
    SubscriptionRepository subscriptionRepository;
    EmployerReviewRepository employerReviewRepository;
    ApplicationRepository applicationRepository;
    RoleRepository roleRepository;
    CategoryRepository categoryRepository;
    JobLevelRepository jobLevelRepository;
    JobTypeRepository jobTypeRepository;
    SubscriptionPlanRepository subscriptionPlanRepository;

    // THÊM CÁC REPOSITORY VÀ MAPPER MỚI
    ChatbotHistoryRepository chatbotHistoryRepository; // Dành cho ChatbotHistory
    ChatbotHistoryMapper chatbotHistoryMapper; // Dành cho mapping ChatbotHistory


    private static final String INTENT_SYSTEM_INSTRUCTION = """
            You are an AI assistant for a job finding platform. Your task is to identify the user's intent and extract all relevant parameters from their query.
            Respond ONLY with a JSON object. Do not add any other text, explanation, or markdown formatting (like ```json).

            Here are the possible intents and their expected JSON structures, along with example queries:

            1.  **job_search**: User wants to find job postings.
                Parameters: `job_title` (string), `location` (string), `min_salary` (float), `max_salary` (float), `category` (string), `job_level` (string), `job_type` (string), `employer_name` (string).
                Example Query: "Tìm việc lập trình viên ở Hà Nội lương từ 1000 đến 2000 đô", "Công việc part-time quản lý ở HCM", "Việc làm IT của FPT", "Tuyển dụng Data Scientist cấp senior"
                JSON: {"intent": "job_search", "jobSearchParams": {"job_title": "string|null", "location": "string|null", "min_salary": float|null, "max_salary": float|null, "category": "string|null", "job_level": "string|null", "job_type": "string|null", "employer_name": "string|null"}}

            2.  **user_info**: User wants to find information about users (job seekers or employers).
                Parameters: `email` (string), `full_name` (string), `role` (string, e.g., "JOB_SEEKER", "EMPLOYER", "ADMIN"), `location` (string), `years_experience` (integer), `is_premium` (boolean).
                Example Query: "Thông tin của người dùng abc@example.com", "Người tìm việc tên Nguyễn Văn A ở Đà Nẵng", "Ai là admin?", "Liệt kê các user premium"
                JSON: {"intent": "user_info", "userSearchParams": {"email": "string|null", "full_name": "string|null", "role": "string|null", "location": "string|null", "years_experience": integer|null, "is_premium": boolean|null}}

            3.  **subscription_info**: User wants to find information about subscription plans or specific user subscriptions.
                Parameters: `user_email` (string), `plan_name` (string), `is_active` (boolean).
                Example Query: "Gói Premium giá bao nhiêu?", "Thông tin gói Standard", "Các gói đăng ký đang hoạt động của user abc@example.com"
                JSON: {"intent": "subscription_info", "subscriptionSearchParams": {"user_email": "string|null", "plan_name": "string|null", "is_active": boolean|null}}

            4.  **company_info**: User wants to find information about a company (based on employer details).
                Parameters: `company_name` (string), `location` (string).
                Example Query: "Thông tin về công ty FPT Software", "Công ty Tech Solutions Inc. ở đâu?", "Mô tả về Global Connect"
                JSON: {"intent": "company_info", "companyInfoParams": {"company_name": "string|null", "location": "string|null"}}

            5.  **employer_reviews**: User wants to find reviews for an employer.
                Parameters: `employer_name` (string), `min_rating` (integer), `max_rating` (integer).
                Example Query: "Đánh giá về FPT Software", "Review công ty Tech Solutions", "Những đánh giá có rating từ 4 sao trở lên cho công ty ABC"
                JSON: {"intent": "employer_reviews", "employerReviewParams": {"employer_name": "string|null", "min_rating": integer|null, "max_rating": integer|null}}

            6.  **application_status**: User wants to find the status of job applications.
                Parameters: `job_title` (string), `job_seeker_email` (string), `status` (string, e.g., "Pending", "Accepted", "Rejected").
                Example Query: "Tình trạng đơn ứng tuyển lập trình viên của Nguyễn Văn A", "Các đơn đã được chấp nhận của user xyz@example.com", "Đơn ứng tuyển nào đang chờ xử lý?"
                JSON: {"intent": "application_status", "applicationSearchParams": {"job_title": "string|null", "job_seeker_email": "string|null", "status": "string|null"}}

            7.  **general_chat**: User query is a general conversation topic, not related to database lookup.
                Parameters: None.
                Example Query: "Chào bạn", "Thời tiết hôm nay thế nào?", "Bạn có khỏe không?"
                JSON: {"intent": "general_chat"}

            8.  **unclear**: User query is ambiguous or cannot be categorized into any of the above intents.
                Parameters: None.
                Example Query: "Tìm kiếm", "Một cái gì đó", "Không hiểu"
                JSON: {"intent": "unclear"}

            If a parameter should be a boolean, explicitly infer true/false. For example, if user asks "liệu có phải tài khoản premium không", set "is_premium": true. If user asks "liệt kê các tài khoản không phải premium", set "is_premium": false.

            Always return a complete JSON object, even if all parameters are null or the intent is 'general_chat'/'unclear'.
            """;

    /**
     * Lấy phản hồi từ chatbot, sử dụng Gemini để phân tích ý định và RAG, có phân quyền,
     * và LƯU LỊCH SỬ TƯƠNG TÁC VÀO DATABASE.
     *
     * @param userMessage Tin nhắn từ người dùng.
     * @return Phản hồi từ chatbot.
     */
    @Transactional // Đảm bảo giao dịch cho việc lưu lịch sử
    public ChatbotHistoryResponse getChatResponse(String userMessage) { // Bỏ authentication khỏi tham số, lấy từ SecurityContextHolder
        log.info("Processing chat message with advanced RAG and authorization: {}", userMessage);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED); // Hoặc bạn có thể cho phép chat mà không cần đăng nhập
        }

        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String currentUserRole = currentUser.getRole().getName();
        log.info("Current user: {} with role: {}", currentUserEmail, currentUserRole);

        String finalGeminiResponse;
        StringBuilder contextForGemini = new StringBuilder(); // Context cho Gemini để tạo câu trả lời cuối cùng

        try {
            // Bước 1: Dùng Gemini để phân tích ý định và trích xuất tham số
            GeminiIntentResponse.IntentAnalysisResult analysisResult =
                    geminiService.analyzeIntent(userMessage, INTENT_SYSTEM_INSTRUCTION);

            String intent = analysisResult.getIntent();
            log.info("Gemini identified intent: {}", intent);

            // --- Logic Phân Quyền và Truy Vấn Dữ Liệu cho từng Intent ---
            switch (intent) {
                case "job_search":
                    // Mọi người dùng đều có thể tìm kiếm công việc
                    GeminiIntentResponse.JobSearchParams searchParams = analysisResult.getJobSearchParams();
                    log.info("Job search params: {}", searchParams);

                    if (searchParams != null) {
                        List<Job> foundJobs = jobRepository.findJobsByCriteria(
                                searchParams.getJobTitle(),
                                searchParams.getLocation(),
                                searchParams.getMinSalary(),
                                searchParams.getMaxSalary(),
                                searchParams.getCategory(),
                                searchParams.getJobLevel(),
                                searchParams.getJobType(),
                                searchParams.getEmployerName()
                        );

                        if (!foundJobs.isEmpty()) {
                            contextForGemini.append("Dưới đây là một số thông tin công việc phù hợp mà tôi tìm thấy: ");
                            String jobListString = foundJobs.stream()
                                    .limit(5) // Giới hạn 5 kết quả cho phản hồi ngắn gọn
                                    .map(job -> {
                                        String companyName = "N/A";
                                        UserDetail employerDetail = userDetailsRepository.findByUserId(job.getEmployer().getId())
                                                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND)); // UserDetail phải tồn tại.;
                                        if (employerDetail != null && employerDetail.getCompanyName() != null) {
                                            companyName = employerDetail.getCompanyName();
                                        }
                                        return String.format("- Vị trí: %s, Công ty: %s, Địa điểm: %s, Lương: %.0f-%.0f, Mô tả: %s...",
                                                job.getTitle(), companyName, job.getLocation(),
                                                job.getSalaryMin(), job.getSalaryMax(),
                                                job.getDescription().substring(0, Math.min(job.getDescription().length(), 100)));
                                    })
                                    .collect(Collectors.joining("\n"));
                            contextForGemini.append("\n").append(jobListString);
                            if (foundJobs.size() > 5) {
                                contextForGemini.append("\n\n(Có thêm nhiều kết quả khác. Bạn có thể thay đổi tiêu chí tìm kiếm để có kết quả chi tiết hơn.)");
                            }
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng một cách hữu ích và giới thiệu các công việc tìm được.");
                            log.info("Found {} jobs. Appending context for Gemini.", foundJobs.size());
                        } else {
                            contextForGemini.append("Tôi không tìm thấy công việc nào phù hợp với yêu cầu của bạn.");
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả và gợi ý tìm kiếm khác hoặc đơn giản hóa yêu cầu.");
                            log.info("No jobs found for the criteria. Appending 'no result' context for Gemini.");
                        }
                    } else {
                        contextForGemini.append("Yêu cầu tìm kiếm công việc của bạn chưa đủ thông tin hoặc không rõ ràng.");
                        contextForGemini.append("\n\nHãy yêu cầu người dùng cung cấp thêm thông tin về vị trí, địa điểm, hoặc ngành nghề.");
                        log.warn("Job search intent detected but no valid search parameters extracted. Appending clarification context.");
                    }
                    break;

                case "user_info":
                    // Chỉ ADMIN hoặc người dùng tìm kiếm thông tin của chính họ mới được phép
                    if (!"ADMIN".equals(currentUserRole)) {
                        GeminiIntentResponse.UserSearchParams userSearchParams = analysisResult.getUserSearchParams();
                        boolean isAskingAboutSelf = userSearchParams != null &&
                                (currentUserEmail.equalsIgnoreCase(userSearchParams.getEmail()) ||
                                        (userSearchParams.getFullName() != null &&
                                                userDetailsRepository.findByUserId(currentUser.getId()).map(UserDetail::getFullName).orElse("").equalsIgnoreCase(userSearchParams.getFullName()))
                                );

                        if (!isAskingAboutSelf) {
                            log.warn("User {} (role {}) attempted to access unauthorized user info.", currentUserEmail, currentUserRole);
                            contextForGemini.append("Xin lỗi, bạn không có quyền truy vấn thông tin cá nhân của người dùng khác.");
                            contextForGemini.append("\n\nHãy thông báo cho người dùng rằng họ không được phép truy cập thông tin này.");
                            break;
                        }
                    }

                    // Nếu là ADMIN hoặc tìm kiếm thông tin của chính mình
                    GeminiIntentResponse.UserSearchParams userSearchParams = analysisResult.getUserSearchParams();
                    log.info("User search params: {}", userSearchParams);

                    if (userSearchParams != null && (userSearchParams.getEmail() != null || userSearchParams.getFullName() != null || userSearchParams.getRole() != null || userSearchParams.getLocation() != null || userSearchParams.getYearsExperience() != null || userSearchParams.getIsPremium() != null || userSearchParams.getIsVerified() != null || userSearchParams.getResumeUrl() != null || userSearchParams.getCompanyName() != null || userSearchParams.getWebsite() != null)) {
                        // Thêm ràng buộc nếu không phải ADMIN, chỉ tìm kiếm chính người dùng đó
                        if (!"ADMIN".equals(currentUserRole)) {
                            userSearchParams.setEmail(currentUserEmail); // Ghi đè để đảm bảo an toàn
                        }

                        List<User> foundUsers = userRepository.findUsersByCriteria(
                                userSearchParams.getEmail(),
                                userSearchParams.getFullName(),
                                userSearchParams.getRole(),
                                userSearchParams.getLocation(),
                                userSearchParams.getYearsExperience(),
                                userSearchParams.getIsPremium(),
                                userSearchParams.getIsVerified(),
                                userSearchParams.getResumeUrl(),
                                userSearchParams.getCompanyName(),
                                userSearchParams.getWebsite()
                        );

                        if (!foundUsers.isEmpty()) {
                            contextForGemini.append("Dưới đây là một số thông tin người dùng phù hợp mà tôi tìm thấy: ");
                            String userListString = foundUsers.stream()
                                    .limit(3) // Giới hạn 3 kết quả
                                    .map(user -> {
                                        String roleName = (user.getRole() != null) ? user.getRole().getName() : "N/A";
                                        UserDetail userDetail = userDetailsRepository.findByUserId(user.getId())
                                                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND)); // UserDetail phải tồn tại.;
                                        String fullName = (userDetail != null && userDetail.getFullName() != null) ? userDetail.getFullName() : "N/A";
                                        String location = (userDetail != null && userDetail.getLocation() != null) ? userDetail.getLocation() : "N/A";
                                        return String.format("- Email: %s, Tên: %s, Vai trò: %s, Địa điểm: %s, Premium: %b",
                                                user.getEmail(), fullName, roleName, location, user.getIsPremium());
                                    })
                                    .collect(Collectors.joining("\n"));
                            contextForGemini.append("\n").append(userListString);
                            if (foundUsers.size() > 3) {
                                contextForGemini.append("\n\n(Có thêm người dùng khác phù hợp với tiêu chí tìm kiếm.)");
                            }
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng một cách hữu ích.");
                            log.info("Found {} users. Appending context for Gemini.", foundUsers.size());
                        } else {
                            contextForGemini.append("Tôi không tìm thấy người dùng nào phù hợp với yêu cầu của bạn.");
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả và gợi ý tìm kiếm khác.");
                            log.info("No users found for the criteria. Appending 'no result' context.");
                        }
                    } else {
                        contextForGemini.append("Yêu cầu tìm kiếm người dùng của bạn chưa đủ thông tin hoặc không rõ ràng.");
                        contextForGemini.append("\n\nHãy yêu cầu người dùng cung cấp email, tên hoặc vai trò cụ thể.");
                        log.warn("User info intent detected but no valid search parameters extracted. Appending clarification context.");
                    }
                    break;

                case "subscription_info":
                    // Chỉ ADMIN hoặc người dùng tìm kiếm thông tin gói của chính họ mới được phép
                    if (!"ADMIN".equals(currentUserRole)) {
                        GeminiIntentResponse.SubscriptionSearchParams subSearchParams = analysisResult.getSubscriptionSearchParams();
                        boolean isAskingAboutSelf = subSearchParams != null && currentUserEmail.equalsIgnoreCase(subSearchParams.getUserEmail());

                        if (!isAskingAboutSelf && subSearchParams != null && subSearchParams.getUserEmail() != null) {
                            log.warn("User {} (role {}) attempted to access unauthorized subscription info for another user: {}", currentUserEmail, currentUserRole, subSearchParams.getUserEmail());
                            contextForGemini.append("Xin lỗi, bạn không có quyền truy vấn thông tin gói đăng ký của người dùng khác.");
                            contextForGemini.append("\n\nHãy thông báo cho người dùng rằng họ không được phép truy cập thông tin này.");
                            break;
                        }
                    }

                    GeminiIntentResponse.SubscriptionSearchParams subSearchParams = analysisResult.getSubscriptionSearchParams();
                    log.info("Subscription search params: {}", subSearchParams);

                    if (subSearchParams != null && (subSearchParams.getUserEmail() != null || subSearchParams.getPlanName() != null || subSearchParams.getIsActive() != null)) {
                        // Nếu không phải ADMIN, và có email người dùng trong params, hãy đảm bảo đó là email của chính họ.
                        if (!"ADMIN".equals(currentUserRole) && subSearchParams.getUserEmail() != null) {
                            subSearchParams.setUserEmail(currentUserEmail); // Ghi đè để đảm bảo an toàn
                        }

                        List<Subscription> foundSubscriptions = subscriptionRepository.findSubscriptionsByCriteria(
                                subSearchParams.getUserEmail(),
                                subSearchParams.getPlanName(),
                                subSearchParams.getIsActive()
                        );

                        if (!foundSubscriptions.isEmpty()) {
                            contextForGemini.append("Dưới đây là thông tin gói đăng ký phù hợp mà tôi tìm thấy: ");
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            String subListString = foundSubscriptions.stream()
                                    .limit(3) // Giới hạn 3 kết quả
                                    .map(sub -> String.format("- Người dùng: %s, Gói: %s, Bắt đầu: %s, Kết thúc: %s, Hoạt động: %b",
                                            sub.getUser().getEmail(), sub.getPlan().getSubscriptionPlanName(),
                                            sub.getStartDate() != null ? sub.getStartDate().format(formatter) : "N/A",
                                            sub.getEndDate() != null ? sub.getEndDate().format(formatter) : "N/A",
                                            sub.getIsActive()))
                                    .collect(Collectors.joining("\n"));
                            contextForGemini.append("\n").append(subListString);
                            if (foundSubscriptions.size() > 3) {
                                contextForGemini.append("\n\n(Có thêm các gói đăng ký khác.)");
                            }
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng một cách hữu ích.");
                            log.info("Found {} subscriptions. Appending context for Gemini.", foundSubscriptions.size());
                        } else {
                            contextForGemini.append("Tôi không tìm thấy thông tin gói đăng ký nào phù hợp với yêu cầu của bạn.");
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả và gợi ý tìm kiếm khác.");
                            log.info("No subscriptions found for the criteria. Appending 'no result' context.");
                        }
                    } else {
                        // Nếu không có tham số nào, có thể liệt kê tất cả các gói đăng ký
                        // Mọi người dùng đều có thể hỏi về các gói đăng ký chung
                        List<SubscriptionPlan> allPlans = subscriptionPlanRepository.findAll();
                        if (!allPlans.isEmpty()) {
                            contextForGemini.append("Dưới đây là các gói đăng ký hiện có trên hệ thống của chúng tôi: ");
                            String planListString = allPlans.stream()
                                    .map(plan -> String.format("- Gói: %s, Giá: %.2f VNĐ, Thời hạn: %d ngày, Đăng bài tối đa: %d, Xem ứng tuyển tối đa: %d, Nổi bật việc làm: %b",
                                            plan.getSubscriptionPlanName(), plan.getPrice(), plan.getDurationDays(),
                                            plan.getMaxJobsPost(), plan.getMaxApplicationsView(), plan.getHighlightJobs()))
                                    .collect(Collectors.joining("\n"));
                            contextForGemini.append("\n").append(planListString);
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng.");
                            log.info("No specific subscription params, listing all plans. Appending context.");
                        } else {
                            contextForGemini.append("Tôi không tìm thấy thông tin gói đăng ký nào. Bạn có thể thử hỏi về một gói cụ thể không?");
                            log.info("No subscription plans found at all. Appending no result context.");
                        }
                    }
                    break;

                case "company_info":
                    // Mọi người dùng đều có thể tìm kiếm thông tin công ty công khai
                    GeminiIntentResponse.CompanyInfoParams companyInfoParams = analysisResult.getCompanyInfoParams();
                    log.info("Company info params: {}", companyInfoParams);

                    if (companyInfoParams != null && companyInfoParams.getCompanyName() != null) {
                        List<UserDetail> foundCompanyDetails;
                        if (companyInfoParams.getLocation() != null) {
                            foundCompanyDetails = userDetailsRepository.findByCompanyNameContainingIgnoreCaseAndLocationContainingIgnoreCase(
                                    companyInfoParams.getCompanyName(), companyInfoParams.getLocation()
                            );
                        } else {
                            foundCompanyDetails = userDetailsRepository.findByCompanyNameContainingIgnoreCase(companyInfoParams.getCompanyName());
                        }

                        if (!foundCompanyDetails.isEmpty()) {
                            contextForGemini.append("Dưới đây là thông tin tôi tìm được về công ty: ");
                            UserDetail companyDetail = foundCompanyDetails.get(0); // Lấy công ty đầu tiên nếu có nhiều
                            contextForGemini.append(String.format("\n- Tên công ty: %s, Địa điểm: %s, Mô tả: %s, Website: %s",
                                    companyDetail.getCompanyName(), companyDetail.getLocation(),
                                    companyDetail.getDescription() != null ? companyDetail.getDescription() : "N/A",
                                    companyDetail.getWebsite() != null ? companyDetail.getWebsite() : "N/A"));

                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng một cách hữu ích.");
                            log.info("Found company info. Appending context for Gemini.");
                        } else {
                            contextForGemini.append("Tôi không tìm thấy thông tin về công ty này.");
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả và gợi ý kiểm tra lại tên công ty.");
                            log.info("No company info found for the criteria. Appending 'no result' context.");
                        }
                    } else {
                        contextForGemini.append("Bạn muốn tìm thông tin về công ty nào? Vui lòng cung cấp tên công ty.");
                        log.warn("Company info intent detected but no company name extracted. Appending clarification context.");
                    }
                    break;

                case "employer_reviews":
                    // Mọi người dùng đều có thể xem đánh giá công khai
                    GeminiIntentResponse.EmployerReviewParams reviewParams = analysisResult.getEmployerReviewParams();
                    log.info("Employer review params: {}", reviewParams);

                    if (reviewParams != null && reviewParams.getEmployerName() != null) {
                        List<UserDetail> employerDetails = userDetailsRepository.findByCompanyNameContainingIgnoreCase(reviewParams.getEmployerName());
                        if (!employerDetails.isEmpty()) {
                            User employerUser = employerDetails.get(0).getUser(); // Lấy đối tượng User từ UserDetail
                            List<EmployerReview> reviews = employerReviewRepository.findEmployerReviewsByEmployerAndRating(
                                    employerUser.getId(), reviewParams.getMinRating(), reviewParams.getMaxRating()
                            );

                            if (!reviews.isEmpty()) {
                                contextForGemini.append(String.format("Dưới đây là một số đánh giá về công ty %s: ", reviewParams.getEmployerName()));
                                String reviewListString = reviews.stream()
                                        .limit(3) // Giới hạn 3 đánh giá
                                        .map(review -> String.format("- Đánh giá: %d sao, Bình luận: \"%s\"",
                                                review.getRating(), review.getComment().substring(0, Math.min(review.getComment().length(), 100)) + "..."))
                                        .collect(Collectors.joining("\n"));
                                contextForGemini.append("\n").append(reviewListString);
                                if (reviews.size() > 3) {
                                    contextForGemini.append("\n\n(Có thêm các đánh giá khác.)");
                                }
                                contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng.");
                                log.info("Found {} employer reviews. Appending context for Gemini.", reviews.size());
                            } else {
                                contextForGemini.append(String.format("Không có đánh giá nào được tìm thấy cho công ty %s với tiêu chí này.", reviewParams.getEmployerName()));
                                contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả.");
                                log.info("No employer reviews found for the criteria. Appending 'no result' context.");
                            }
                        } else {
                            contextForGemini.append(String.format("Tôi không tìm thấy công ty '%s' trong hệ thống để tìm đánh giá.", reviewParams.getEmployerName()));
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy công ty.");
                            log.info("Employer company name not found for review search. Appending 'company not found' context.");
                        }
                    } else {
                        contextForGemini.append("Bạn muốn xem đánh giá về công ty nào? Vui lòng cung cấp tên công ty.");
                        log.warn("Employer review intent detected but no employer name extracted. Appending clarification context.");
                    }
                    break;

                case "application_status":
                    // Phân quyền cho application_status:
                    // Job Seeker chỉ được xem đơn ứng tuyển của chính mình.
                    // Employer chỉ được xem đơn ứng tuyển vào các công việc của họ.
                    // Admin có thể xem tất cả.
                    GeminiIntentResponse.ApplicationSearchParams appSearchParams = analysisResult.getApplicationSearchParams();
                    log.info("Application search params: {}", appSearchParams);

                    List<Application> foundApplications = null;

                    if (appSearchParams != null && (appSearchParams.getJobSeekerEmail() != null || appSearchParams.getJobTitle() != null || appSearchParams.getStatus() != null)) {
                        if ("JOB_SEEKER".equals(currentUserRole)) {
                            // Job Seeker chỉ có thể hỏi về đơn ứng tuyển của chính mình
                            if (appSearchParams.getJobSeekerEmail() != null && !currentUserEmail.equalsIgnoreCase(appSearchParams.getJobSeekerEmail())) {
                                log.warn("Job Seeker {} attempted to access application status of another user: {}", currentUserEmail, appSearchParams.getJobSeekerEmail());
                                contextForGemini.append("Xin lỗi, bạn chỉ có thể xem tình trạng đơn ứng tuyển của chính mình.");
                                contextForGemini.append("\n\nHãy thông báo cho người dùng rằng họ không được phép truy cập thông tin này.");
                                break; // Dừng xử lý intent này
                            }
                            // Ghi đè email của Job Seeker để đảm bảo an toàn
                            appSearchParams.setJobSeekerEmail(currentUserEmail);
                            foundApplications = applicationRepository.findApplicationsByJobSeekerAndJobTitleAndStatus(
                                    currentUser.getId(), appSearchParams.getJobTitle(), appSearchParams.getStatus());

                        } else if ("EMPLOYER".equals(currentUserRole)) {
                            // Employer chỉ có thể xem đơn ứng tuyển vào các công việc của họ
                            // Cần lấy tất cả job IDs của employer này và tìm kiếm ứng tuyển
                            List<Long> employerJobIds = jobRepository.findByEmployerId(currentUser.getId()) // Giả định có phương thức này
                                    .stream().map(Job::getId).collect(Collectors.toList());

                            if (employerJobIds.isEmpty()) {
                                contextForGemini.append("Bạn chưa đăng công việc nào nên không có đơn ứng tuyển để hiển thị.");
                                contextForGemini.append("\n\nHãy trả lời người dùng rằng không có công việc nào được đăng.");
                                break;
                            }
                            foundApplications = applicationRepository.findApplicationsByJobIdsAndJobTitleAndStatus(
                                    employerJobIds, appSearchParams.getJobTitle(), appSearchParams.getStatus()
                            );

                        } else if ("ADMIN".equals(currentUserRole)) {
                            // Admin có thể xem tất cả
                            // Nếu có email job seeker trong params, tìm user đó
                            Long jobSeekerId = null;
                            if (appSearchParams.getJobSeekerEmail() != null) {
                                User jobSeeker = userRepository.findByEmail(appSearchParams.getJobSeekerEmail())
                                        .orElse(null);
                                if (jobSeeker != null) {
                                    jobSeekerId = jobSeeker.getId();
                                }
                            }
                            foundApplications = applicationRepository.findApplicationsByJobSeekerAndJobTitleAndStatus(
                                    jobSeekerId, appSearchParams.getJobTitle(), appSearchParams.getStatus());
                        } else {
                            // Role không được hỗ trợ cho intent này
                            contextForGemini.append("Xin lỗi, vai trò của bạn không được phép truy vấn thông tin đơn ứng tuyển.");
                            contextForGemini.append("\n\nHãy thông báo cho người dùng rằng họ không được phép.");
                            break;
                        }

                        // Xử lý kết quả tìm thấy
                        if (foundApplications != null && !foundApplications.isEmpty()) {
                            contextForGemini.append("Dưới đây là một số đơn ứng tuyển phù hợp: ");
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            String appListString = foundApplications.stream()
                                    .limit(5) // Giới hạn 5 kết quả
                                    .map(app -> String.format("- Vị trí: %s, Người ứng tuyển: %s, Trạng thái: %s, Ngày ứng tuyển: %s",
                                            app.getJob().getTitle(),
                                            app.getJobSeeker().getEmail(),
                                            app.getStatus(),
                                            app.getAppliedAt() != null ? app.getAppliedAt().format(formatter) : "N/A"))
                                    .collect(Collectors.joining("\n"));
                            contextForGemini.append("\n").append(appListString);
                            if (foundApplications.size() > 5) {
                                contextForGemini.append("\n\n(Có thêm các đơn ứng tuyển khác.)");
                            }
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng.");
                            log.info("Found {} applications. Appending context for Gemini.", foundApplications.size());
                        } else {
                            contextForGemini.append("Không tìm thấy đơn ứng tuyển nào phù hợp với yêu cầu của bạn.");
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả và gợi ý tìm kiếm khác.");
                            log.info("No applications found for the criteria. Appending 'no result' context.");
                        }
                    } else {
                        contextForGemini.append("Yêu cầu tìm kiếm trạng thái đơn ứng tuyển của bạn chưa đủ thông tin hoặc không rõ ràng.");
                        contextForGemini.append("\n\nHãy yêu cầu người dùng cung cấp thêm thông tin về vị trí công việc, email người ứng tuyển hoặc trạng thái.");
                        log.warn("Application status intent detected but no valid search parameters extracted. Appending clarification context.");
                    }
                    break;

                case "general_chat":
                    contextForGemini.append("Đây là một cuộc trò chuyện chung. Hãy trả lời câu hỏi của người dùng một cách tự nhiên và thân thiện.");
                    log.info("General chat intent detected. Appending general context.");
                    break;

                case "unclear":
                default:
                    contextForGemini.append("Xin lỗi, tôi không thể hiểu rõ yêu cầu của bạn. Vui lòng thử lại với một câu hỏi rõ ràng hơn về tìm kiếm việc làm, thông tin người dùng, gói đăng ký, công ty, đánh giá, hoặc trạng thái ứng tuyển.");
                    contextForGemini.append("\n\nHãy thông báo cho người dùng rằng bạn không hiểu yêu cầu và gợi ý các chủ đề mà bạn có thể hỗ trợ.");
                    log.warn("Unclear intent detected or unknown intent. Appending clarification context.");
                    break;
            }

            // Bước 2: Dùng Gemini để tạo câu trả lời cuối cùng dựa trên user_message và contextForGemini
            // Truyền cả userMessage và contextForGemini vào Gemini để nó có đủ thông tin tạo câu trả lời phù hợp
            finalGeminiResponse = geminiService.generateResponseWithContext(userMessage, contextForGemini.toString());
            log.info("Gemini generated final response: {}", finalGeminiResponse);

        } catch (IOException e) {
            log.error("Error communicating with Gemini API: {}", e.getMessage(), e);
            finalGeminiResponse = "Xin lỗi, có lỗi xảy ra khi tôi cố gắng kết nối với hệ thống. Vui lòng thử lại sau.";
            throw new AppException(ErrorCode.GEMINI_API_ERROR); // Hoặc một ErrorCode cụ thể hơn
        } catch (Exception e) {
            log.error("An unexpected error occurred during chat processing: {}", e.getMessage(), e);
            finalGeminiResponse = "Xin lỗi, có lỗi nội bộ xảy ra. Vui lòng thử lại sau.";
            throw new AppException(ErrorCode.UNEXPECTED_ERROR); // Xử lý các lỗi khác
        }

        // Bước 3: Lưu lịch sử cuộc trò chuyện vào database
        try {
            ChatbotHistory chatbotHistory = ChatbotHistory.builder()
                    .user(currentUser)
                    .message(userMessage)
                    .response(finalGeminiResponse)
                    .build();
            // createdAt sẽ được set tự động bởi @PrePersist
            chatbotHistoryRepository.save(chatbotHistory);
            log.info("Chatbot history saved successfully for user {}.", currentUserEmail);
        } catch (Exception e) {
            log.error("Failed to save chatbot history for user {}: {}", currentUserEmail, e.getMessage(), e);
            // Có thể ném một AppException khác nếu việc lưu lịch sử là bắt buộc
            // Hoặc chỉ log lỗi và vẫn trả về phản hồi cho người dùng
        }


        // Trả về phản hồi cho client
        return ChatbotHistoryResponse.builder()
                .response(finalGeminiResponse)
                .build();
    }

    @Transactional
    public ChatbotHistoryResponse sendMessageToChatbot(ChatbotMessageRequest request) {
        String userMessage = request.getMessage();
        log.info("Processing chat message with advanced RAG and authorization: {}", userMessage);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String currentUserRole = currentUser.getRole().getName();
        log.info("Current user: {} with role: {}", currentUserEmail, currentUserRole);

        String finalGeminiResponse;
        StringBuilder contextForGemini = new StringBuilder();
        ChatbotHistory savedChatbotHistory = null;

        try {
            GeminiIntentResponse.IntentAnalysisResult analysisResult =
                    geminiService.analyzeIntent(userMessage, INTENT_SYSTEM_INSTRUCTION);

            String intent = analysisResult.getIntent();
            log.info("Gemini identified intent: {}", intent);

            switch (intent) {
                case "job_search":
                    GeminiIntentResponse.JobSearchParams searchParams = analysisResult.getJobSearchParams();
                    if (searchParams != null) {
                        List<Job> foundJobs = jobRepository.findJobsByCriteria(
                                searchParams.getJobTitle(), searchParams.getLocation(),
                                searchParams.getMinSalary(), searchParams.getMaxSalary(),
                                searchParams.getCategory(), searchParams.getJobLevel(),
                                searchParams.getJobType(), searchParams.getEmployerName());

                        if (!foundJobs.isEmpty()) {
                            contextForGemini.append("Dưới đây là một số thông tin công việc phù hợp mà tôi tìm thấy: ");
                            String jobListString = foundJobs.stream()
                                    .limit(5)
                                    .map(job -> {
                                        String companyName = userDetailsRepository.findByUserId(job.getEmployer().getId())
                                                .map(UserDetail::getCompanyName).orElse("N/A");
                                        return String.format("- Vị trí: %s, Công ty: %s, Địa điểm: %s, Lương: %.0f-%.0f, Mô tả: %s...",
                                                job.getTitle(), companyName, job.getLocation(),
                                                job.getSalaryMin(), job.getSalaryMax(),
                                                job.getDescription().substring(0, Math.min(job.getDescription().length(), 100)));
                                    }).collect(Collectors.joining("\n"));
                            contextForGemini.append("\n").append(jobListString);
                            if (foundJobs.size() > 5) contextForGemini.append("\n\n(Có thêm nhiều kết quả khác.)");
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng một cách hữu ích và giới thiệu các công việc tìm được.");
                        } else {
                            contextForGemini.append("Tôi không tìm thấy công việc nào phù hợp với yêu cầu của bạn.");
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả và gợi ý tìm kiếm khác.");
                        }
                    } else {
                        contextForGemini.append("Yêu cầu tìm kiếm công việc của bạn chưa đủ thông tin hoặc không rõ ràng.");
                        contextForGemini.append("\n\nHãy yêu cầu người dùng cung cấp thêm thông tin.");
                    }
                    break;

                case "user_info":
                    if (!"ADMIN".equals(currentUserRole)) {
                        GeminiIntentResponse.UserSearchParams userSearchParams = analysisResult.getUserSearchParams();
                        boolean isAskingAboutSelf = userSearchParams != null &&
                                (currentUserEmail.equalsIgnoreCase(userSearchParams.getEmail()) ||
                                        (userSearchParams.getFullName() != null &&
                                                userDetailsRepository.findByUserId(currentUser.getId()).map(UserDetail::getFullName).orElse("").equalsIgnoreCase(userSearchParams.getFullName()))
                                );
                        if (!isAskingAboutSelf) {
                            log.warn("User {} (role {}) attempted to access unauthorized user info.", currentUserEmail, currentUserRole);
                            contextForGemini.append("Xin lỗi, bạn không có quyền truy vấn thông tin cá nhân của người dùng khác.");
                            contextForGemini.append("\n\nHãy thông báo cho người dùng rằng họ không được phép truy cập thông tin này.");
                            break;
                        }
                    }
                    GeminiIntentResponse.UserSearchParams userSearchParams = analysisResult.getUserSearchParams();
                    if (userSearchParams != null) {
                        if (!"ADMIN".equals(currentUserRole)) userSearchParams.setEmail(currentUserEmail);
                        List<User> foundUsers = userRepository.findUsersByCriteria(
                                userSearchParams.getEmail(), userSearchParams.getFullName(),
                                userSearchParams.getRole(), userSearchParams.getLocation(),
                                userSearchParams.getYearsExperience(), userSearchParams.getIsPremium(),
                                userSearchParams.getIsVerified(), userSearchParams.getResumeUrl(),
                                userSearchParams.getCompanyName(), userSearchParams.getWebsite());
                        if (!foundUsers.isEmpty()) {
                            contextForGemini.append("Dưới đây là một số thông tin người dùng phù hợp mà tôi tìm thấy: ");
                            String userListString = foundUsers.stream().limit(3)
                                    .map(user -> {
                                        String roleName = (user.getRole() != null) ? user.getRole().getName() : "N/A";
                                        UserDetail userDetail = userDetailsRepository.findByUserId(user.getId())
                                                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

                                        String fullName = (userDetail != null && userDetail.getFullName() != null) ? userDetail.getFullName() : "N/A";
                                        return String.format("- Email: %s, Tên: %s, Vai trò: %s, Địa điểm: %s, Premium: %b",
                                                user.getEmail(), fullName, roleName, (userDetail != null ? userDetail.getLocation() : "N/A"), user.getIsPremium());
                                    }).collect(Collectors.joining("\n"));
                            contextForGemini.append("\n").append(userListString);
                            if (foundUsers.size() > 3) contextForGemini.append("\n\n(Có thêm người dùng khác.)");
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng một cách hữu ích.");
                        } else {
                            contextForGemini.append("Tôi không tìm thấy người dùng nào phù hợp với yêu cầu của bạn.");
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả và gợi ý tìm kiếm khác.");
                        }
                    } else {
                        contextForGemini.append("Yêu cầu tìm kiếm người dùng của bạn chưa đủ thông tin hoặc không rõ ràng.");
                        contextForGemini.append("\n\nHãy yêu cầu người dùng cung cấp email, tên hoặc vai trò cụ thể.");
                    }
                    break;

                case "subscription_info":
                    if (!"ADMIN".equals(currentUserRole)) {
                        GeminiIntentResponse.SubscriptionSearchParams subSearchParams = analysisResult.getSubscriptionSearchParams();
                        boolean isAskingAboutSelf = subSearchParams != null && currentUserEmail.equalsIgnoreCase(subSearchParams.getUserEmail());
                        if (!isAskingAboutSelf && subSearchParams != null && subSearchParams.getUserEmail() != null) {
                            log.warn("User {} (role {}) attempted to access unauthorized subscription info for another user: {}", currentUserEmail, currentUserRole, subSearchParams.getUserEmail());
                            contextForGemini.append("Xin lỗi, bạn không có quyền truy vấn thông tin gói đăng ký của người dùng khác.");
                            contextForGemini.append("\n\nHãy thông báo cho người dùng rằng họ không được phép truy cập thông tin này.");
                            break;
                        }
                    }
                    GeminiIntentResponse.SubscriptionSearchParams subSearchParams = analysisResult.getSubscriptionSearchParams();
                    if (subSearchParams != null && (subSearchParams.getUserEmail() != null || subSearchParams.getPlanName() != null || subSearchParams.getIsActive() != null)) {
                        if (!"ADMIN".equals(currentUserRole) && subSearchParams.getUserEmail() != null) subSearchParams.setUserEmail(currentUserEmail);
                        List<Subscription> foundSubscriptions = subscriptionRepository.findSubscriptionsByCriteria(
                                subSearchParams.getUserEmail(), subSearchParams.getPlanName(), subSearchParams.getIsActive());
                        if (!foundSubscriptions.isEmpty()) {
                            contextForGemini.append("Dưới đây là thông tin gói đăng ký phù hợp mà tôi tìm thấy: ");
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            String subListString = foundSubscriptions.stream().limit(3)
                                    .map(sub -> String.format("- Người dùng: %s, Gói: %s, Bắt đầu: %s, Kết thúc: %s, Hoạt động: %b",
                                            sub.getUser().getEmail(), sub.getPlan().getSubscriptionPlanName(),
                                            sub.getStartDate() != null ? sub.getStartDate().format(formatter) : "N/A",
                                            sub.getEndDate() != null ? sub.getEndDate().format(formatter) : "N/A", sub.getIsActive()))
                                    .collect(Collectors.joining("\n"));
                            contextForGemini.append("\n").append(subListString);
                            if (foundSubscriptions.size() > 3) contextForGemini.append("\n\n(Có thêm các gói đăng ký khác.)");
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng một cách hữu ích.");
                        } else {
                            contextForGemini.append("Tôi không tìm thấy thông tin gói đăng ký nào phù hợp với yêu cầu của bạn.");
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả và gợi ý tìm kiếm khác.");
                        }
                    } else {
                        List<SubscriptionPlan> allPlans = subscriptionPlanRepository.findAll();
                        if (!allPlans.isEmpty()) {
                            contextForGemini.append("Dưới đây là các gói đăng ký hiện có trên hệ thống của chúng tôi: ");
                            String planListString = allPlans.stream()
                                    .map(plan -> String.format("- Gói: %s, Giá: %.2f VNĐ, Thời hạn: %d ngày, Đăng bài tối đa: %d, Xem ứng tuyển tối đa: %d, Nổi bật việc làm: %b",
                                            plan.getSubscriptionPlanName(), plan.getPrice(), plan.getDurationDays(),
                                            plan.getMaxJobsPost(), plan.getMaxApplicationsView(), plan.getHighlightJobs()))
                                    .collect(Collectors.joining("\n"));
                            contextForGemini.append("\n").append(planListString);
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng.");
                        } else {
                            contextForGemini.append("Tôi không tìm thấy thông tin gói đăng ký nào. Bạn có thể thử hỏi về một gói cụ thể không?");
                        }
                    }
                    break;

                case "company_info":
                    GeminiIntentResponse.CompanyInfoParams companyInfoParams = analysisResult.getCompanyInfoParams();
                    if (companyInfoParams != null && companyInfoParams.getCompanyName() != null) {
                        List<UserDetail> foundCompanyDetails = companyInfoParams.getLocation() != null ?
                                userDetailsRepository.findByCompanyNameContainingIgnoreCaseAndLocationContainingIgnoreCase(
                                        companyInfoParams.getCompanyName(), companyInfoParams.getLocation()) :
                                userDetailsRepository.findByCompanyNameContainingIgnoreCase(companyInfoParams.getCompanyName());
                        if (!foundCompanyDetails.isEmpty()) {
                            UserDetail companyDetail = foundCompanyDetails.get(0);
                            contextForGemini.append("Dưới đây là thông tin tôi tìm được về công ty: ");
                            contextForGemini.append(String.format("\n- Tên công ty: %s, Địa điểm: %s, Mô tả: %s, Website: %s",
                                    companyDetail.getCompanyName(), companyDetail.getLocation(),
                                    companyDetail.getDescription() != null ? companyDetail.getDescription() : "N/A",
                                    companyDetail.getWebsite() != null ? companyDetail.getWebsite() : "N/A"));
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng một cách hữu ích.");
                        } else {
                            contextForGemini.append("Tôi không tìm thấy thông tin về công ty này.");
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả và gợi ý kiểm tra lại tên công ty.");
                        }
                    } else {
                        contextForGemini.append("Bạn muốn tìm thông tin về công ty nào? Vui lòng cung cấp tên công ty.");
                    }
                    break;

                case "employer_reviews":
                    GeminiIntentResponse.EmployerReviewParams reviewParams = analysisResult.getEmployerReviewParams();
                    if (reviewParams != null && reviewParams.getEmployerName() != null) {
                        List<UserDetail> employerDetails = userDetailsRepository.findByCompanyNameContainingIgnoreCase(reviewParams.getEmployerName());
                        if (!employerDetails.isEmpty()) {
                            User employerUser = employerDetails.get(0).getUser();
                            List<EmployerReview> reviews = employerReviewRepository.findEmployerReviewsByEmployerAndRating(
                                    employerUser.getId(), reviewParams.getMinRating(), reviewParams.getMaxRating());
                            if (!reviews.isEmpty()) {
                                contextForGemini.append(String.format("Dưới đây là một số đánh giá về công ty %s: ", reviewParams.getEmployerName()));
                                String reviewListString = reviews.stream().limit(3)
                                        .map(review -> String.format("- Đánh giá: %d sao, Bình luận: \"%s\"",
                                                review.getRating(), review.getComment().substring(0, Math.min(review.getComment().length(), 100)) + "..."))
                                        .collect(Collectors.joining("\n"));
                                contextForGemini.append("\n").append(reviewListString);
                                if (reviews.size() > 3) contextForGemini.append("\n\n(Có thêm các đánh giá khác.)");
                                contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng.");
                            } else {
                                contextForGemini.append(String.format("Không có đánh giá nào được tìm thấy cho công ty %s với tiêu chí này.", reviewParams.getEmployerName()));
                                contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả.");
                            }
                        } else {
                            contextForGemini.append(String.format("Tôi không tìm thấy công ty '%s' trong hệ thống để tìm đánh giá.", reviewParams.getEmployerName()));
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy công ty.");
                        }
                    } else {
                        contextForGemini.append("Bạn muốn xem đánh giá về công ty nào? Vui lòng cung cấp tên công ty.");
                    }
                    break;

                case "application_status":
                    GeminiIntentResponse.ApplicationSearchParams appSearchParams = analysisResult.getApplicationSearchParams();
                    List<Application> foundApplications = null;
                    if (appSearchParams != null && (appSearchParams.getJobSeekerEmail() != null || appSearchParams.getJobTitle() != null || appSearchParams.getStatus() != null)) {
                        if ("JOB_SEEKER".equals(currentUserRole)) {
                            if (appSearchParams.getJobSeekerEmail() != null && !currentUserEmail.equalsIgnoreCase(appSearchParams.getJobSeekerEmail())) {
                                log.warn("Job Seeker {} attempted to access application status of another user: {}", currentUserEmail, appSearchParams.getJobSeekerEmail());
                                contextForGemini.append("Xin lỗi, bạn chỉ có thể xem tình trạng đơn ứng tuyển của chính mình.");
                                contextForGemini.append("\n\nHãy thông báo cho người dùng rằng họ không được phép truy cập thông tin này.");
                                break;
                            }
                            appSearchParams.setJobSeekerEmail(currentUserEmail);
                            foundApplications = applicationRepository.findApplicationsByJobSeekerAndJobTitleAndStatus(
                                    currentUser.getId(), appSearchParams.getJobTitle(), appSearchParams.getStatus()); // dùng getStatus()
                        } else if ("EMPLOYER".equals(currentUserRole)) {
                            List<Long> employerJobIds = jobRepository.findByEmployerId(currentUser.getId())
                                    .stream().map(Job::getId).collect(Collectors.toList());
                            if (employerJobIds.isEmpty()) {
                                contextForGemini.append("Bạn chưa đăng công việc nào nên không có đơn ứng tuyển để hiển thị.");
                                contextForGemini.append("\n\nHãy trả lời người dùng rằng không có công việc nào được đăng.");
                                break;
                            }
                            foundApplications = applicationRepository.findApplicationsByJobIdsAndJobTitleAndStatus(
                                    employerJobIds, appSearchParams.getJobTitle(), appSearchParams.getStatus()); // dùng getStatus()
                        } else if ("ADMIN".equals(currentUserRole)) {
                            Long jobSeekerId = null;
                            if (appSearchParams.getJobSeekerEmail() != null) {
                                User jobSeeker = userRepository.findByEmail(appSearchParams.getJobSeekerEmail()).orElse(null);
                                if (jobSeeker != null) jobSeekerId = jobSeeker.getId();
                            }
                            foundApplications = applicationRepository.findApplicationsByJobSeekerAndJobTitleAndStatus(
                                    jobSeekerId, appSearchParams.getJobTitle(), appSearchParams.getStatus()); // dùng getStatus()
                        } else {
                            contextForGemini.append("Xin lỗi, vai trò của bạn không được phép truy vấn thông tin đơn ứng tuyển.");
                            contextForGemini.append("\n\nHãy thông báo cho người dùng rằng họ không được phép.");
                            break;
                        }

                        if (foundApplications != null && !foundApplications.isEmpty()) {
                            contextForGemini.append("Dưới đây là một số đơn ứng tuyển phù hợp: ");
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            String appListString = foundApplications.stream().limit(5)
                                    .map(app -> String.format("- Vị trí: %s, Người ứng tuyển: %s, Trạng thái: %s, Ngày ứng tuyển: %s",
                                            app.getJob().getTitle(), app.getJobSeeker().getEmail(),
                                            app.getStatus(),
                                            app.getAppliedAt() != null ? app.getAppliedAt().format(formatter) : "N/A"))
                                    .collect(Collectors.joining("\n"));
                            contextForGemini.append("\n").append(appListString);
                            if (foundApplications.size() > 5) contextForGemini.append("\n\n(Có thêm các đơn ứng tuyển khác.)");
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng.");
                        } else {
                            contextForGemini.append("Không tìm thấy đơn ứng tuyển nào phù hợp với yêu cầu của bạn.");
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả và gợi ý tìm kiếm khác.");
                        }
                    } else {
                        contextForGemini.append("Yêu cầu tìm kiếm trạng thái đơn ứng tuyển của bạn chưa đủ thông tin hoặc không rõ ràng.");
                        contextForGemini.append("\n\nHãy yêu cầu người dùng cung cấp thêm thông tin.");
                    }
                    break;

                case "general_chat":
                    contextForGemini.append("Đây là một cuộc trò chuyện chung. Hãy trả lời câu hỏi của người dùng một cách tự nhiên và thân thiện.");
                    break;

                case "unclear":
                default:
                    contextForGemini.append("Xin lỗi, tôi không thể hiểu rõ yêu cầu của bạn. Vui lòng thử lại với một câu hỏi rõ ràng hơn.");
                    contextForGemini.append("\n\nHãy thông báo cho người dùng rằng bạn không hiểu yêu cầu và gợi ý các chủ đề mà bạn có thể hỗ trợ.");
                    break;
            }

            finalGeminiResponse = geminiService.generateResponseWithContext(userMessage, contextForGemini.toString());

        } catch (IOException e) {
            log.error("Error communicating with Gemini API: {}", e.getMessage(), e);
            finalGeminiResponse = "Xin lỗi, có lỗi xảy ra khi tôi cố gắng kết nối với hệ thống. Vui lòng thử lại sau.";
            throw new AppException(ErrorCode.GEMINI_API_ERROR);
        } catch (Exception e) {
            log.error("An unexpected error occurred during chat processing: {}", e.getMessage(), e);
            finalGeminiResponse = "Xin lỗi, có lỗi nội bộ xảy ra. Vui lòng thử lại sau.";
            throw new AppException(ErrorCode.UNEXPECTED_ERROR);
        }

        try {
            ChatbotHistory chatbotHistoryToSave = ChatbotHistory.builder()
                    .user(currentUser)
                    .message(userMessage)
                    .response(finalGeminiResponse)
                    .build();
            savedChatbotHistory = chatbotHistoryRepository.save(chatbotHistoryToSave);
            log.info("Chatbot history saved successfully for user {}. ID: {}", currentUserEmail, savedChatbotHistory.getId());
        } catch (Exception e) {
            log.error("Failed to save chatbot history for user {}: {}", currentUserEmail, e.getMessage(), e);
            // Quyết định: Nếu không lưu được lịch sử, bạn có muốn trả về lỗi cho người dùng không?
            // Hoặc bạn vẫn muốn trả về phản hồi Gemini nhưng với thông báo lỗi nhẹ?
            // Tạm thời, tôi sẽ ném lỗi nếu không lưu được history vì bạn muốn trả về history.
            throw new AppException(ErrorCode.FAILED_TO_SAVE_CHAT_HISTORY); // Bạn cần định nghĩa ErrorCode này
        }

        // Chuyển đổi đối tượng ChatbotHistory đã được lưu (với ID và createdAt) thành ChatbotHistoryResponse
        return chatbotHistoryMapper.toChatbotHistoryResponse(savedChatbotHistory);
    }


    /**
     * Lấy lịch sử trò chuyện của người dùng hiện tại.
     *
     * @return Danh sách các ChatbotHistoryResponse của người dùng hiện tại.
     */
    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'EMPLOYER', 'ADMIN')") // Chỉ cho phép người dùng đã xác thực
    public List<ChatbotHistoryResponse> getMyChatbotHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<ChatbotHistory> userHistory = chatbotHistoryRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());

        log.info("Found {} chat history entries for user {}.", userHistory.size(), currentUserEmail);
        return userHistory.stream()
                .map(chatbotHistoryMapper::toChatbotHistoryResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'EMPLOYER', 'ADMIN')")
    public ChatbotHistoryResponse getChatbotHistoryById(Long historyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String currentUserRole = currentUser.getRole().getName();

        ChatbotHistory history = chatbotHistoryRepository.findById(historyId)
                .orElseThrow(() -> new AppException(ErrorCode.CHATBOT_HISTORY_NOT_FOUND));

        // Kiểm tra quyền truy cập
        if ("ADMIN".equals(currentUserRole) || history.getUser().getId().equals(currentUser.getId())) {
            log.info("User {} (role {}) accessed chat history ID {}.", currentUserEmail, currentUserRole, historyId);
            return chatbotHistoryMapper.toChatbotHistoryResponse(history);
        } else {
            log.warn("User {} (role {}) attempted to access unauthorized chat history ID {}.", currentUserEmail, currentUserRole, historyId);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }
    /**
     * Xóa một bản ghi lịch sử trò chuyện cụ thể theo ID.
     * Người dùng chỉ có thể xóa lịch sử của chính họ, ADMIN có thể xóa bất kỳ lịch sử nào.
     *
     * @param historyId ID của bản ghi lịch sử trò chuyện cần xóa.
     * @throws AppException Nếu không tìm thấy lịch sử hoặc người dùng không có quyền.
     */
    @Transactional
    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'EMPLOYER', 'ADMIN')")
    public void deleteChatbotHistory(Long historyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String currentUserRole = currentUser.getRole().getName();

        ChatbotHistory history = chatbotHistoryRepository.findById(historyId)
                .orElseThrow(() -> new AppException(ErrorCode.CHATBOT_HISTORY_NOT_FOUND));

        if ("ADMIN".equals(currentUserRole) || history.getUser().getId().equals(currentUser.getId())) {
            chatbotHistoryRepository.delete(history);
            log.info("User {} (role {}) deleted chat history ID {}.", currentUserEmail, currentUserRole, historyId);
        } else {
            log.warn("User {} (role {}) attempted to delete unauthorized chat history ID {}.", currentUserEmail, currentUserRole, historyId);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }


    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới có quyền này
    public List<ChatbotHistoryResponse> getAllChatbotHistoryForAdmin() {
        // Logging và xác thực vai trò đã được @PreAuthorize xử lý
        log.info("Admin user is requesting all chatbot history.");
        List<ChatbotHistory> allHistory = chatbotHistoryRepository.findAllByOrderByCreatedAtDesc();

        return allHistory.stream()
                .map(chatbotHistoryMapper::toChatbotHistoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional // Đảm bảo giao dịch được quản lý
    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'EMPLOYER', 'ADMIN')") // Chỉ người dùng đã đăng nhập mới có thể xóa lịch sử của họ
    public void clearMyChatbotHistory() {
        // 1. Lấy thông tin người dùng hiện tại từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED); // Đảm bảo người dùng đã xác thực
        }

        String currentUserEmail = authentication.getName();

        // 2. Tìm người dùng trong database
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 3. Xóa tất cả lịch sử chatbot liên quan đến người dùng này
        // Sử dụng phương thức deleteByUserId trong Repository
        chatbotHistoryRepository.deleteByUserId(currentUser.getId());

        log.info("Successfully cleared all chatbot history for user: {}", currentUserEmail);
    }


}