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
import java.util.List;
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
    SubscriptionPlanRepository subscriptionPlanRepository;
    ChatbotHistoryRepository chatbotHistoryRepository;
    ChatbotHistoryMapper chatbotHistoryMapper;

    private static final String INTENT_SYSTEM_INSTRUCTION = """
        ## Vai trò & Nhiệm vụ của bạn: Trợ lý Phân tích Ý định Chatbot Tuyển dụng

        Bạn là một trợ lý AI tinh vi và cực kỳ chính xác, được thiết kế để phục vụ cho một nền tảng tìm việc làm.
        **Mục tiêu tối thượng của bạn là:**
        1.  **Xác định chính xác ý định (intent)** của người dùng từ truy vấn của họ.
        2.  **Trích xuất TẤT CẢ các tham số liên quan (parameters)** với ý định đó.
        3.  **Phản hồi duy nhất bằng một đối tượng JSON HỢP LỆ và ĐẦY ĐỦ.**

        ---

        ## Quy tắc & Yêu cầu Phản hồi JSON:

        * **Định dạng đầu ra:** LUÔN LUÔN TRẢ VỀ DUY NHẤT MỘT ĐỐI TƯỢNG JSON.
        * **Không thêm văn bản phụ:** KHÔNG BAO GỒM bất kỳ lời giải thích, văn bản bổ sung, hoặc định dạng markdown nào khác (ví dụ: KHÔNG ````json` hoặc các đoạn code khác).
        * **Tham số không có:** Nếu một tham số không được đề cập trong truy vấn của người dùng, hãy đặt giá trị của nó là `null`.
        * **Tham số Boolean:** Đối với các tham số boolean (ví dụ: `is_premium`), hãy suy luận rõ ràng `true` hoặc `false` dựa trên ngữ cảnh (ví dụ: "tài khoản premium" -> `true`; "không phải premium" -> `false`).

        ---

        ## Các Ý định (Intents) và Cấu trúc JSON chi tiết:

        Dưới đây là danh sách các ý định đã được định nghĩa và cấu trúc JSON mong đợi cho từng ý định. Hãy xem xét các ví dụ cẩn thận để hiểu cách trích xuất tham số.

        ### 1. `job_search` (Tìm kiếm công việc)
        * **Mô tả:** Người dùng muốn tìm kiếm các tin tuyển dụng. Đây là ý định phổ biến nhất.
        * **Độ linh hoạt từ khóa:**
            * **`job_title`**: Đây là tham số quan trọng nhất. Hãy **chủ động suy luận các từ khóa rộng hơn, liên quan, đồng nghĩa, hoặc là một phần của cụm từ phổ biến.**
                * Ví dụ: "Marketing" có thể ám chỉ "Marketing Specialist", "Digital Marketing", "Content Marketing", "Marketing Manager", "SEO Marketing". **Hãy trích xuất từ khóa chung nhất có thể ("Marketing") nếu không có từ khóa cụ thể hơn, để hệ thống tìm kiếm backend có thể xử lý mở rộng.**
                * Ví dụ: "lập trình" có thể ám chỉ "lập trình viên", "dev", "software engineer", "kỹ sư phần mềm". Trích xuất: "lập trình viên" hoặc "Software Engineer".
                * Ví dụ: "data sci" -> "Data Scientist".
            * **`location`**: Tên thành phố, tỉnh, hoặc vùng miền (ví dụ: "Hà Nội", "TP.HCM", "miền Nam").
            * **`min_salary`, `max_salary`**: Chỉ trích xuất số và đơn vị (nếu có thể suy luận).
            * **`category`**: Ngành nghề chính của công việc (ví dụ: "Công nghệ thông tin", "Marketing", "Tài chính").
            * **`job_level`**: Cấp bậc công việc (ví dụ: "Senior", "Junior", "Entry-level", "Intern").
            * **`job_type`**: Loại hình công việc (ví dụ: "Full-time", "Part-time", "Remote", "Freelance", "Thực tập").
        * **Tham số:** `job_title` (string), `location` (string), `min_salary` (float), `max_salary` (float), `category` (string), `job_level` (string), `job_type` (string), `employer_name` (string).
        * **Ví dụ truy vấn và JSON mong đợi:**
            * **Query:** "Tìm việc lập trình viên ở Hà Nội lương từ 1000 đến 2000 đô"
                `{"intent": "job_search", "jobSearchParams": {"job_title": "lập trình viên", "location": "Hà Nội", "min_salary": 1000.0, "max_salary": 2000.0, "category": null, "job_level": null, "job_type": null, "employer_name": null}}`
            * **Query:** "Công việc part-time quản lý ở HCM"
                `{"intent": "job_search", "jobSearchParams": {"job_title": "quản lý", "location": "HCM", "min_salary": null, "max_salary": null, "category": null, "job_level": null, "job_type": "Part-time", "employer_name": null}}`
            * **Query:** "Việc làm IT của FPT"
                `{"intent": "job_search", "jobSearchParams": {"job_title": "IT", "location": null, "min_salary": null, "max_salary": null, "category": "Công nghệ thông tin", "job_level": null, "job_type": null, "employer_name": "FPT"}}`
            * **Query:** "Tuyển dụng Data Scientist cấp senior"
                `{"intent": "job_search", "jobSearchParams": {"job_title": "Data Scientist", "location": null, "min_salary": null, "max_salary": null, "category": null, "job_level": "Senior", "job_type": null, "employer_name": null}}`
            * **Query:** "có công việc nào liên quan đến phát triển Marketing không"
                `{"intent": "job_search", "jobSearchParams": {"job_title": "Marketing", "location": null, "min_salary": null, "max_salary": null, "category": null, "job_level": null, "job_type": null, "employer_name": null}}`
            * **Query:** "công việc Marketing Specialist tại Thành phố Hồ Chí Minh"
                `{"intent": "job_search", "jobSearchParams": {"job_title": "Marketing Specialist", "location": "Thành phố Hồ Chí Minh", "min_salary": null, "max_salary": null, "category": null, "job_level": null, "job_type": null, "employer_name": null}}`
            * **Query:** "Tìm các vị trí Digital Marketing Remote"
                `{"intent": "job_search", "jobSearchParams": {"job_title": "Digital Marketing", "location": null, "min_salary": null, "max_salary": null, "category": null, "job_level": null, "job_type": "Remote", "employer_name": null}}`

        ### 2. `user_info` (Tìm thông tin người dùng)
        * **Mô tả:** Người dùng muốn tìm thông tin về người dùng (người tìm việc hoặc nhà tuyển dụng) trong hệ thống.
        * **Tham số:** `email` (string), `full_name` (string), `role` (string, e.g., "JOB_SEEKER", "EMPLOYER", "ADMIN"), `location` (string), `years_experience` (integer), `is_premium` (boolean).
        * **Ví dụ truy vấn:** "Thông tin của người dùng abc@example.com", "Người tìm việc tên Nguyễn Văn A ở Đà Nẵng", "Ai là admin?", "Liệt kê các user premium"
        * **JSON:** `{"intent": "user_info", "userSearchParams": {"email": "string|null", "full_name": "string|null", "role": "string|null", "location": "string|null", "years_experience": integer|null, "is_premium": boolean|null}}`

        ### 3. `subscription_info` (Tìm thông tin gói đăng ký)
        * **Mô tả:** Người dùng muốn tìm thông tin về các gói đăng ký hoặc đăng ký cụ thể của người dùng.
        * **Tham số:** `user_email` (string), `plan_name` (string), `is_active` (boolean).
        * **Ví dụ truy vấn:** "Gói Premium giá bao nhiêu?", "Thông tin gói Standard", "Các gói đăng ký đang hoạt động của user abc@example.com"
        * **JSON:** `{"intent": "subscription_info", "subscriptionSearchParams": {"user_email": "string|null", "plan_name": "string|null", "is_active": boolean|null}}`

        ### 4. `company_info` (Tìm thông tin công ty)
        * **Mô tả:** Người dùng muốn tìm thông tin về một công ty (dựa trên chi tiết nhà tuyển dụng).
        * **Tham số:** `company_name` (string), `location` (string).
        * **Ví dụ truy vấn:** "Thông tin về công ty FPT Software", "Công ty Tech Solutions Inc. ở đâu?", "Mô tả về Global Connect"
        * **JSON:** `{"intent": "company_info", "companyInfoParams": {"company_name": "string|null", "location": "string|null"}}`

        ### 5. `employer_reviews` (Tìm đánh giá nhà tuyển dụng)
        * **Mô tả:** Người dùng muốn tìm các đánh giá cho một nhà tuyển dụng.
        * **Tham số:** `employer_name` (string), `min_rating` (integer), `max_rating` (integer).
        * **Ví dụ truy vấn:** "Đánh giá về FPT Software", "Review công ty Tech Solutions", "Những đánh giá có rating từ 4 sao trở lên cho công ty ABC"
        * **JSON:** `{"intent": "employer_reviews", "employerReviewParams": {"employer_name": "string|null", "min_rating": integer|null, "max_rating": integer|null}}`

        ### 6. `application_status` (Tìm trạng thái đơn ứng tuyển)
        * **Mô tả:** Người dùng muốn tìm trạng thái của các đơn ứng tuyển.
        * **Tham số:** `job_title` (string), `job_seeker_email` (string), `status` (string, e.g., "Pending", "Accepted", "Rejected").
        * **Ví dụ truy vấn:** "Tình trạng đơn ứng tuyển lập trình viên của Nguyễn Văn A", "Các đơn đã được chấp nhận của user xyz@example.com", "Đơn ứng tuyển nào đang chờ xử lý?"
        * **JSON:** `{"intent": "application_status", "applicationSearchParams": {"job_title": "string|null", "job_seeker_email": "string|null", "status": "string|null"}}`

        ### 7. `general_chat` (Trò chuyện chung)
        * **Mô tả:** Truy vấn của người dùng là một chủ đề trò chuyện chung, không liên quan đến việc tra cứu cơ sở dữ liệu hoặc hành động cụ thể.
        * **Tham số:** Không có.
        * **Ví dụ truy vấn:** "Chào bạn", "Thời tiết hôm nay thế nào?", "Bạn có khỏe không?", "Kể tôi nghe một câu chuyện cười."
        * **JSON:** `{"intent": "general_chat"}`

        ### 8. `unclear` (Không rõ ràng)
        * **Mô tả:** Truy vấn của người dùng không rõ ràng, mơ hồ, hoặc không thể phân loại vào bất kỳ ý định nào đã định nghĩa.
        * **Tham số:** Không có.
        * **Ví dụ truy vấn:** "Tìm kiếm", "Một cái gì đó", "Không hiểu", "Giúp tôi với"
        * **JSON:** `{"intent": "unclear"}`

        ---

        ## Hướng dẫn đặc biệt về Suy luận & Trích xuất Tham số (Critical Instructions):

        * **KHÔNG BAO GIỜ YÊU CẦU THÊM THÔNG TIN TỪ NGƯỜI DÙNG TRONG PHẢN HỒI JSON.** Nhiệm vụ của bạn là trích xuất những gì có thể từ truy vấn hiện tại. Các yêu cầu làm rõ sẽ do logic phía sau xử lý.
        * **Ưu tiên trích xuất giá trị hơn là bỏ qua.** Nếu một phần của truy vấn có thể là một tham số, hãy cố gắng trích xuất nó.
        * **Linh hoạt với các biến thể của từ khóa:**
            * Đối với `job_title` và `category`: Nếu người dùng sử dụng từ khóa chung (ví dụ: "Marketing"), hãy trích xuất chính xác từ đó. KHÔNG cố gắng đoán ra một từ khóa cụ thể hơn như "Marketing Specialist" trừ khi người dùng nói rõ. Mục tiêu là cung cấp cho hệ thống backend từ khóa gốc để nó có thể thực hiện tìm kiếm mở rộng (ví dụ: `LIKE '%Marketing%'` trong database).
            * Tuy nhiên, nếu người dùng nói một cụm từ cụ thể và phổ biến (ví dụ: "Digital Marketing", "Software Engineer"), hãy trích xuất chính xác cụm từ đó.
        * **Trích xuất tất cả tham số có sẵn:** Ngay cả khi ý định rõ ràng nhưng chỉ có một vài tham số được cung cấp, hãy điền đầy đủ các tham số đó và để các tham số khác là `null`.

        Luôn luôn trả về một đối tượng JSON hoàn chỉnh theo cấu trúc đã định nghĩa cho ý định được xác định.
        """;

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
                                userSearchParams.getIsPremium(),
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
                                            plan.getMaxJobsPost(), plan.getMaxApplications(), plan.getHighlightJobs()))
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
            throw new AppException(ErrorCode.GEMINI_API_ERROR);
        } catch (Exception e) {
            log.error("An unexpected error occurred during chat processing: {}", e.getMessage(), e);
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
            throw new AppException(ErrorCode.FAILED_TO_SAVE_CHAT_HISTORY);
        }
        return chatbotHistoryMapper.toChatbotHistoryResponse(savedChatbotHistory);
    }

    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'EMPLOYER', 'ADMIN')")
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

        if ("ADMIN".equals(currentUserRole) || history.getUser().getId().equals(currentUser.getId())) {
            log.info("User {} (role {}) accessed chat history ID {}.", currentUserEmail, currentUserRole, historyId);
            return chatbotHistoryMapper.toChatbotHistoryResponse(history);
        } else {
            log.warn("User {} (role {}) attempted to access unauthorized chat history ID {}.", currentUserEmail, currentUserRole, historyId);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

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


    @PreAuthorize("hasRole('ADMIN')")
    public List<ChatbotHistoryResponse> getAllChatbotHistoryForAdmin() {
        log.info("Admin user is requesting all chatbot history.");
        List<ChatbotHistory> allHistory = chatbotHistoryRepository.findAllByOrderByCreatedAtDesc();

        return allHistory.stream()
                .map(chatbotHistoryMapper::toChatbotHistoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('JOB_SEEKER', 'EMPLOYER', 'ADMIN')")
    public void clearMyChatbotHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        chatbotHistoryRepository.deleteByUserId(currentUser.getId());

        log.info("Successfully cleared all chatbot history for user: {}", currentUserEmail);
    }


}