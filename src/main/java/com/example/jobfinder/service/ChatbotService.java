package com.example.jobfinder.service;

import com.example.jobfinder.dto.chat.ChatResponse;
import com.example.jobfinder.dto.gemini.GeminiIntentResponse;
import com.example.jobfinder.model.*; // Import tất cả các model bạn cần
import com.example.jobfinder.repository.*; // Import tất cả các repository bạn cần
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter; // Để format ngày tháng

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatbotService {

    GeminiService geminiService;
    JobRepository jobRepository;
    UserRepository userRepository;
    UserDetailsRepository userDetailsRepository; // Để tìm thông tin công ty từ employer details
    SubscriptionRepository subscriptionRepository;
    EmployerReviewRepository employerReviewRepository; // Cần tạo repo này
    ApplicationRepository applicationRepository; // Cần tạo repo này
    RoleRepository roleRepository; // Nếu cần lấy role object
    CategoryRepository categoryRepository; // Nếu cần lấy category object
    JobLevelRepository jobLevelRepository; // Nếu cần lấy jobLevel object
    JobTypeRepository jobTypeRepository; // Nếu cần lấy jobType object
    SubscriptionPlanRepository subscriptionPlanRepository;

    // System instruction cho Gemini để phân tích ý định và trích xuất thông tin
    // RẤT QUAN TRỌNG: Prompt này phải rõ ràng, bao gồm ví dụ cụ thể để Gemini hiểu.
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
     * Lấy phản hồi từ chatbot, sử dụng Gemini để phân tích ý định và RAG.
     *
     * @param userMessage Tin nhắn từ người dùng.
     * @return Phản hồi từ chatbot.
     */
    public ChatResponse getChatResponse(String userMessage) {
        log.info("Processing chat message with advanced RAG: {}", userMessage);

        try {
            // Bước 1: Dùng Gemini để phân tích ý định và trích xuất tham số
            GeminiIntentResponse.IntentAnalysisResult analysisResult =
                    geminiService.analyzeIntent(userMessage, INTENT_SYSTEM_INSTRUCTION);

            String intent = analysisResult.getIntent();
            String finalGeminiResponse;
            StringBuilder contextForGemini = new StringBuilder();

            log.info("Gemini identified intent: {}", intent);

            switch (intent) {
                case "job_search":
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
                                    .limit(5) // Giới hạn số lượng kết quả để prompt không quá dài
                                    .map(job -> String.format("- Vị trí: %s, Công ty: %s, Địa điểm: %s, Lương: %.0f-%.0f, Mô tả: %s...",
                                            job.getTitle(), userDetailsRepository.findByUserId(job.getEmployer().getId()).getCompanyName(), job.getLocation(),
                                            job.getSalaryMin(), job.getSalaryMax(),
                                            job.getDescription().substring(0, Math.min(job.getDescription().length(), 100))))
                                    .collect(Collectors.joining("\n"));
                            contextForGemini.append("\n").append(jobListString);
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng một cách hữu ích và giới thiệu các công việc tìm được. Nếu có nhiều kết quả, chỉ cần liệt kê 5 cái đầu tiên và nói rằng có thể tìm thêm.");
                            log.info("Found {} jobs. Appending context.", foundJobs.size());
                        } else {
                            contextForGemini.append("Tôi không tìm thấy công việc nào phù hợp với yêu cầu của bạn.");
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả và gợi ý tìm kiếm khác hoặc đơn giản hóa yêu cầu.");
                            log.info("No jobs found for the criteria. Appending 'no result' context.");
                        }
                    } else {
                        contextForGemini.append("Yêu cầu tìm kiếm công việc của bạn chưa đủ thông tin hoặc không rõ ràng.");
                        contextForGemini.append("\n\nHãy yêu cầu người dùng cung cấp thêm thông tin về vị trí, địa điểm, hoặc ngành nghề.");
                        log.warn("Job search intent detected but no valid search parameters extracted.");
                    }
                    break;

                case "user_info":
                    GeminiIntentResponse.UserSearchParams userSearchParams = analysisResult.getUserSearchParams();
                    log.info("User search params: {}", userSearchParams);

                    if (userSearchParams != null && (userSearchParams.getEmail() != null || userSearchParams.getFullName() != null || userSearchParams.getRole() != null || userSearchParams.getLocation() != null || userSearchParams.getYearsExperience() != null || userSearchParams.getIsPremium() != null)) {
                        List<User> foundUsers = userRepository.findUsersByCriteria(
                                userSearchParams.getEmail(),
                                userSearchParams.getFullName(),
                                userSearchParams.getRole(),
                                userSearchParams.getLocation(),
                                userSearchParams.getYearsExperience(),
                                userSearchParams.getIsPremium(),
                                userSearchParams.getIsVerified()
                        );

                        if (!foundUsers.isEmpty()) {
                            contextForGemini.append("Dưới đây là một số thông tin người dùng phù hợp mà tôi tìm thấy: ");
                            String userListString = foundUsers.stream()
                                    .limit(3) // Giới hạn số lượng kết quả
                                    .map(user -> {
                                        String roleName = (user.getRole() != null) ? user.getRole().getName() : "N/A";

                                        String fullName = ( userDetailsRepository.findByUserId(user.getId()) != null &&  userDetailsRepository.findByUserId(user.getId()).getFullName() != null) ?  userDetailsRepository.findByUserId(user.getId()).getFullName() : "N/A";
                                        String location = ( userDetailsRepository.findByUserId(user.getId()) != null &&  userDetailsRepository.findByUserId(user.getId()).getLocation() != null) ?  userDetailsRepository.findByUserId(user.getId()).getLocation() : "N/A";
                                        return String.format("- Email: %s, Tên: %s, Vai trò: %s, Địa điểm: %s",
                                                user.getEmail(), fullName, roleName, location);
                                    })
                                    .collect(Collectors.joining("\n"));
                            contextForGemini.append("\n").append(userListString);
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng một cách hữu ích.");
                            log.info("Found {} users. Appending context.", foundUsers.size());
                        } else {
                            contextForGemini.append("Tôi không tìm thấy người dùng nào phù hợp với yêu cầu của bạn.");
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả và gợi ý tìm kiếm khác.");
                            log.info("No users found for the criteria.");
                        }
                    } else {
                        contextForGemini.append("Yêu cầu tìm kiếm người dùng của bạn chưa đủ thông tin hoặc không rõ ràng.");
                        contextForGemini.append("\n\nHãy yêu cầu người dùng cung cấp email, tên hoặc vai trò cụ thể.");
                        log.warn("User info intent detected but no valid search parameters extracted.");
                    }
                    break;

                case "subscription_info":
                    GeminiIntentResponse.SubscriptionSearchParams subSearchParams = analysisResult.getSubscriptionSearchParams();
                    log.info("Subscription search params: {}", subSearchParams);

                    if (subSearchParams != null && (subSearchParams.getUserEmail() != null || subSearchParams.getPlanName() != null || subSearchParams.getIsActive() != null)) {
                        List<Subscription> foundSubscriptions = subscriptionRepository.findSubscriptionsByCriteria(
                                subSearchParams.getUserEmail(),
                                subSearchParams.getPlanName(),
                                subSearchParams.getIsActive()
                        );

                        if (!foundSubscriptions.isEmpty()) {
                            contextForGemini.append("Dưới đây là thông tin gói đăng ký phù hợp mà tôi tìm thấy: ");
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            String subListString = foundSubscriptions.stream()
                                    .limit(3)
                                    .map(sub -> String.format("- Người dùng: %s, Gói: %s, Bắt đầu: %s, Kết thúc: %s, Hoạt động: %b",
                                            sub.getUser().getEmail(), sub.getPlan().getSubscriptionPlanName(),
                                            sub.getStartDate() != null ? sub.getStartDate().format(formatter) : "N/A",
                                            sub.getEndDate() != null ? sub.getEndDate().format(formatter) : "N/A",
                                            sub.getIsActive()))
                                    .collect(Collectors.joining("\n"));
                            contextForGemini.append("\n").append(subListString);
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng một cách hữu ích.");
                            log.info("Found {} subscriptions. Appending context.", foundSubscriptions.size());
                        } else {
                            contextForGemini.append("Tôi không tìm thấy thông tin gói đăng ký nào phù hợp với yêu cầu của bạn.");
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả và gợi ý tìm kiếm khác.");
                            log.info("No subscriptions found for the criteria.");
                        }
                    } else {
                        // Nếu không có tham số nào, có thể liệt kê tất cả các gói đăng ký
                        List<SubscriptionPlan> allPlans = subscriptionPlanRepository.findAll();
                        if (!allPlans.isEmpty()) {
                            contextForGemini.append("Dưới đây là các gói đăng ký hiện có trên hệ thống của chúng tôi: ");
                            String planListString = allPlans.stream()
                                    .map(plan -> String.format("- Gói: %s, Giá: %.2f, Thời hạn: %d ngày, Đăng bài tối đa: %d, Xem ứng tuyển tối đa: %d, Nổi bật việc làm: %b",
                                            plan.getSubscriptionPlanName(), plan.getPrice(), plan.getDurationDays(),
                                            plan.getMaxJobsPost(), plan.getMaxApplicationsView(), plan.getHighlightJobs()))
                                    .collect(Collectors.joining("\n"));
                            contextForGemini.append("\n").append(planListString);
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng.");
                            log.info("No specific subscription params, listing all plans.");
                        } else {
                            contextForGemini.append("Tôi không tìm thấy thông tin gói đăng ký nào. Bạn có thể thử hỏi về một gói cụ thể không?");
                            log.info("No subscription plans found at all.");
                        }
                    }
                    break;

                case "company_info":
                    GeminiIntentResponse.CompanyInfoParams companyInfoParams = analysisResult.getCompanyInfoParams();
                    log.info("Company info params: {}", companyInfoParams);

                    if (companyInfoParams != null && companyInfoParams.getCompanyName() != null) {
                        List<UserDetail> foundCompanyDetails;
                        if (companyInfoParams.getLocation() != null) {
                            foundCompanyDetails = userDetailsRepository.findByCompanyNameContainingIgnoreCaseAndLocationContainingIgnoreCase(
                                    companyInfoParams.getCompanyName(), companyInfoParams.getLocation()
                            );
                        } else {
                            foundCompanyDetails = userDetailsRepository.findByCompanyNameContainingIgnoreCase(companyInfoParams.getCompanyName()).stream().collect(Collectors.toList());
                        }


                        if (!foundCompanyDetails.isEmpty()) {
                            contextForGemini.append("Dưới đây là thông tin tôi tìm được về công ty: ");
                            // Lấy thông tin từ user_details của Employer
                            UserDetail companyDetail = foundCompanyDetails.get(0); // Lấy cái đầu tiên
                            contextForGemini.append(String.format("\n- Tên công ty: %s, Địa điểm: %s, Mô tả: %s, Website: %s",
                                    companyDetail.getCompanyName(), companyDetail.getLocation(),
                                    companyDetail.getDescription() != null ? companyDetail.getDescription() : "N/A",
                                    companyDetail.getWebsite() != null ? companyDetail.getWebsite() : "N/A"));

                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng một cách hữu ích.");
                            log.info("Found company info. Appending context.");
                        } else {
                            contextForGemini.append("Tôi không tìm thấy thông tin về công ty này.");
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả và gợi ý kiểm tra lại tên công ty.");
                            log.info("No company info found for the criteria.");
                        }
                    } else {
                        contextForGemini.append("Bạn muốn tìm thông tin về công ty nào? Vui lòng cung cấp tên công ty.");
                        log.warn("Company info intent detected but no company name extracted.");
                    }
                    break;

                case "employer_reviews":
                    GeminiIntentResponse.EmployerReviewParams reviewParams = analysisResult.getEmployerReviewParams();
                    log.info("Employer review params: {}", reviewParams);

                    if (reviewParams != null && reviewParams.getEmployerName() != null) {
                        List<UserDetail> employerDetails = userDetailsRepository.findByCompanyNameContainingIgnoreCase(reviewParams.getEmployerName());
                        if (!employerDetails.isEmpty()) {
                            User employerUser = employerDetails.get(0).getUser();
                            List<EmployerReview> reviews = employerReviewRepository.findEmployerReviewsByEmployerAndRating(
                                    employerUser.getId(), reviewParams.getMinRating(), reviewParams.getMaxRating()
                            );

                            if (!reviews.isEmpty()) {
                                contextForGemini.append(String.format("Dưới đây là một số đánh giá về công ty %s: ", reviewParams.getEmployerName()));
                                String reviewListString = reviews.stream()
                                        .limit(3)
                                        .map(review -> String.format("- Đánh giá: %d sao, Bình luận: \"%s\"",
                                                review.getRating(), review.getComment().substring(0, Math.min(review.getComment().length(), 100)) + "..."))
                                        .collect(Collectors.joining("\n"));
                                contextForGemini.append("\n").append(reviewListString);
                                contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng.");
                                log.info("Found {} employer reviews.", reviews.size());
                            } else {
                                contextForGemini.append(String.format("Không có đánh giá nào được tìm thấy cho công ty %s với tiêu chí này.", reviewParams.getEmployerName()));
                                contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả.");
                                log.info("No employer reviews found for the criteria.");
                            }
                        } else {
                            contextForGemini.append(String.format("Tôi không tìm thấy công ty '%s' trong hệ thống để tìm đánh giá.", reviewParams.getEmployerName()));
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy công ty.");
                            log.info("Employer company name not found for review search.");
                        }
                    } else {
                        contextForGemini.append("Bạn muốn xem đánh giá về công ty nào? Vui lòng cung cấp tên công ty.");
                        log.warn("Employer review intent detected but no employer name extracted.");
                    }
                    break;

                case "application_status":
                    GeminiIntentResponse.ApplicationSearchParams appSearchParams = analysisResult.getApplicationSearchParams();
                    log.info("Application search params: {}", appSearchParams);

                    if (appSearchParams != null && (appSearchParams.getJobSeekerEmail() != null || appSearchParams.getJobTitle() != null || appSearchParams.getStatus() != null)) {
                        List<Application> foundApplications = applicationRepository.findApplicationsByCriteria(
                                appSearchParams.getJobSeekerEmail(),
                                appSearchParams.getJobTitle(),
                                appSearchParams.getStatus()
                        );

                        if (!foundApplications.isEmpty()) {
                            contextForGemini.append("Dưới đây là tình trạng các đơn ứng tuyển tôi tìm thấy: ");
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                            String appListString = foundApplications.stream()
                                    .limit(5)
                                    .map(app -> String.format("- Ứng viên: %s, Công việc: %s, Trạng thái: %s, Ứng tuyển lúc: %s",
                                            app.getJobSeeker().getEmail(), app.getJob().getTitle(),
                                            app.getStatus(), app.getAppliedAt().format(formatter)))
                                    .collect(Collectors.joining("\n"));
                            contextForGemini.append("\n").append(appListString);
                            contextForGemini.append("\n\nDựa vào thông tin này, hãy trả lời câu hỏi của người dùng.");
                            log.info("Found {} applications.", foundApplications.size());
                        } else {
                            contextForGemini.append("Tôi không tìm thấy đơn ứng tuyển nào phù hợp với yêu cầu của bạn.");
                            contextForGemini.append("\n\nHãy trả lời người dùng rằng không tìm thấy kết quả và gợi ý tìm kiếm khác.");
                            log.info("No applications found for the criteria.");
                        }
                    } else {
                        contextForGemini.append("Bạn muốn xem tình trạng đơn ứng tuyển nào? Vui lòng cung cấp thêm thông tin.");
                        log.warn("Application status intent detected but insufficient params extracted.");
                    }
                    break;

                case "general_chat":
                    log.info("Intent: general_chat. No specific database query needed.");
                    // Không cần thêm context, Gemini sẽ trả lời chung chung
                    break;

                case "unclear":
                default:
                    log.warn("Intent unclear or not supported: {}", intent);
                    contextForGemini.append("Tôi không chắc chắn về ý định của bạn. ");
                    contextForGemini.append("\n\nHãy cố gắng trả lời câu hỏi của người dùng tốt nhất có thể dựa trên ngữ cảnh chung.");
                    break;
            }

            // Bước cuối: Xây dựng Prompt và Gọi Gemini
            String promptToGemini;
            if (contextForGemini.length() > 0) {
                promptToGemini = String.format("Người dùng hỏi: \"%s\".\n\n%s", userMessage, contextForGemini.toString());
            } else {
                promptToGemini = userMessage; // Nếu không có context đặc biệt, gửi nguyên bản
            }

            finalGeminiResponse = geminiService.getGeminiResponse(promptToGemini);
            log.debug("Final Gemini response for user message '{}': {}", userMessage, finalGeminiResponse);

            return new ChatResponse(finalGeminiResponse);

        } catch (IOException e) {
            log.error("Error communicating with Gemini service during RAG process: {}", e.getMessage(), e);
            return new ChatResponse("Xin lỗi, tôi đang gặp vấn đề kỹ thuật khi giao tiếp với AI. Vui lòng thử lại sau.");
        } catch (Exception e) {
            log.error("An unexpected error occurred in ChatbotService during RAG process: {}", e.getMessage(), e);
            return new ChatResponse("Đã xảy ra lỗi không xác định trong quá trình xử lý. Vui lòng thử lại.");
        }
    }
}