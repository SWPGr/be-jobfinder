package com.example.jobfinder.service;

import com.example.jobfinder.dto.ai.JobseekerAnalysisAIDTO;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.Application; // Giữ lại nếu hàm summarizeResumeWithGemini vẫn dùng Application
import com.example.jobfinder.model.JobseekerAnalysis;
import com.example.jobfinder.model.UserDetail;
import com.example.jobfinder.repository.ApplicationRepository; // Giữ lại nếu hàm summarizeResumeWithGemini vẫn dùng Application
import com.example.jobfinder.repository.JobseekerAnalysisRepository;
import com.example.jobfinder.repository.UserDetailsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobseekerAnalysisService {

    // Giữ lại ApplicationRepository nếu hàm summarizeResumeWithGemini vẫn cần
    private final ApplicationRepository applicationRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    private final JobseekerAnalysisRepository jobseekerAnalysisRepository;
    private final UserDetailsRepository userDetailsRepository; // Dù không dùng trực tiếp ở đây, vẫn giữ vì có thể cần ở các service khác

    @Value("${google.gemini.model-name}")
    private String geminiModelUsedForAnalysis;

    /**
     * Tóm tắt nội dung resume bằng Gemini cho một Application cụ thể.
     * Lưu ý: Hàm này vẫn liên kết với Application. Nếu mục tiêu là phân tích resume của UserDetail,
     * hãy ưu tiên sử dụng analyzeAndSaveJobseekerResume(UserDetail userDetail).
     *
     * @param applicationId ID của ứng dụng
     * @return Chuỗi tóm tắt resume từ Gemini
     * @throws IOException Nếu có lỗi trong quá trình đọc PDF
     * @throws AppException Nếu không tìm thấy Application, resume URL hoặc nội dung resume trống
     */
    @Transactional(readOnly = true)
    public String summarizeResumeWithGemini(Long applicationId) throws IOException {
        log.info("Attempting to summarize resume for Application ID: {}", applicationId);
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        String resumeUrl = application.getResume();
        if (resumeUrl == null || resumeUrl.trim().isEmpty()) {
            log.warn("Resume URL not found for Application ID: {}", applicationId);
            throw new AppException(ErrorCode.RESUME_NOT_FOUND_FOR_APPLICATION);
        }

        String resumeContent = extractPdfContent(resumeUrl);

        if (resumeContent.trim().isEmpty()) {
            log.warn("Extracted resume content is empty for Application ID: {}", applicationId);
            throw new AppException(ErrorCode.EMPTY_RESUME_CONTENT);
        }

        String prompt = """
            Bạn là một chuyên gia tóm tắt hồ sơ ứng viên.
            Hãy đọc kỹ và tóm tắt nội dung resume dưới đây một cách chi tiết nhưng súc tích, tập trung vào những điểm chính sau:
            - **Thông tin liên hệ cơ bản**: Tên, email, số điện thoại (nếu có).
            - **Mục tiêu nghề nghiệp/Tóm tắt bản thân**: Tóm tắt ngắn gọn nếu có.
            - **Kinh nghiệm làm việc**:
                - Liệt kê các vị trí công việc gần đây nhất (tối đa 3 vị trí).
                - Với mỗi vị trí, nêu tên công ty, chức danh, thời gian làm việc và 1-2 gạch đầu dòng mô tả các trách nhiệm chính hoặc thành tựu nổi bật nhất.
            - **Kỹ năng**:
                - Phân loại và liệt kê các kỹ năng chính (ví dụ: Ngôn ngữ lập trình, Frameworks, Cơ sở dữ liệu, Công cụ, Kỹ năng mềm).
                - Chỉ liệt kê các kỹ năng được đề cập rõ ràng trong resume.
            - **Học vấn**: Liệt kê bằng cấp cao nhất, tên trường và thời gian tốt nghiệp.
            - **Dự án/Hoạt động (nếu có)**: Tóm tắt 1-2 dự án hoặc hoạt động nổi bật, nêu rõ vai trò và kết quả chính.
            
            Đảm bảo tóm tắt bằng tiếng Việt, mạch lạc, chuyên nghiệp và không thêm thông tin suy diễn.
            Nếu một phần thông tin không có trong resume, hãy bỏ qua phần đó.
            
            --- Bắt đầu Resume ---
            """ + resumeContent + """
            --- Kết thúc Resume ---
            """;

        log.info("Sending summary prompt to Gemini for Application ID: {}", applicationId);
        return geminiService.getGeminiResponse(prompt);
    }

    /**
     * Phân tích và lưu trữ chi tiết resume của ứng viên bằng AI, liên kết với UserDetail.
     * Đây là hàm chính để xử lý phân tích resume profile của job seeker.
     *
     * @param userDetail Đối tượng UserDetail của job seeker cần phân tích resume.
     * @return Đối tượng JobseekerAnalysis đã được lưu vào DB.
     * @throws IOException Nếu có lỗi trong quá trình đọc PDF hoặc giao tiếp AI.
     * @throws AppException Nếu không tìm thấy resume URL, nội dung resume trống, hoặc lỗi AI.
     */
    @Transactional
    public JobseekerAnalysis analyzeAndSaveJobseekerResume(UserDetail userDetail) throws IOException {
        log.info("Starting AI analysis for Jobseeker resume. UserDetail ID: {}", userDetail.getId());

        String resumeUrl = userDetail.getResumeUrl();
        if (resumeUrl == null || resumeUrl.trim().isEmpty()) {
            log.warn("Resume URL not found for UserDetail ID: {}", userDetail.getId());
            throw new AppException(ErrorCode.RESUME_NOT_FOUND_FOR_APPLICATION); // Có thể tạo ErrorCode.RESUME_NOT_FOUND_FOR_USER_DETAIL
        }

        String resumeContent = extractPdfContent(resumeUrl);

        if (resumeContent.trim().isEmpty()) {
            log.warn("Extracted resume content is empty for UserDetail ID: {}", userDetail.getId());
            throw new AppException(ErrorCode.EMPTY_RESUME_CONTENT);
        }

        String prompt = buildJobseekerAnalysisPrompt(resumeContent);
        String rawGeminiResponseText;

        try {
            log.info("Sending detailed analysis prompt to Gemini for UserDetail ID: {}", userDetail.getId());
            rawGeminiResponseText = geminiService.getGeminiResponse(prompt);

            String jsonString = extractJsonFromString(rawGeminiResponseText);
            if (jsonString.isEmpty()) {
                log.error("Could not extract JSON from Gemini response for UserDetail ID: {}", userDetail.getId());
                throw new AppException(ErrorCode.AI_PARSE_ERROR);
            }

            JobseekerAnalysisAIDTO aiResult = objectMapper.readValue(jsonString, JobseekerAnalysisAIDTO.class);

            Optional<JobseekerAnalysis> existingAnalysisOpt = jobseekerAnalysisRepository.findByUserDetail(userDetail);
            JobseekerAnalysis jobseekerAnalysis;

            if (existingAnalysisOpt.isPresent()) {
                jobseekerAnalysis = existingAnalysisOpt.get();
                log.info("Updating existing jobseeker analysis for UserDetail ID: {}", userDetail.getId());
                // Khi update, userDetail đã được thiết lập bởi Hibernate khi lấy từ DB
            } else {
                jobseekerAnalysis = new JobseekerAnalysis();
                jobseekerAnalysis.setUserDetail(userDetail); // *** ĐÂY LÀ DÒNG KHẮC PHỤC LỖI "user_detail_id cannot be null" ***
                log.info("Creating new jobseeker analysis for UserDetail ID: {}", userDetail.getId());
            }

            // Gán các trường phân tích từ AI Result DTO vào Entity
            jobseekerAnalysis.setFullName(aiResult.getFullName());
            jobseekerAnalysis.setEmail(aiResult.getEmail());
            jobseekerAnalysis.setPhoneNumber(aiResult.getPhoneNumber());
            jobseekerAnalysis.setCareerObjectiveSummary(aiResult.getCareerObjectiveSummary());
            jobseekerAnalysis.setWorkExperienceSummary(aiResult.getWorkExperienceSummary());
            jobseekerAnalysis.setSkillsSummary(aiResult.getSkillsSummary());
            jobseekerAnalysis.setEducationSummary(aiResult.getEducationSummary());
            jobseekerAnalysis.setProjectsActivitiesSummary(aiResult.getProjectsActivitiesSummary());
            jobseekerAnalysis.setCertificationsAwardsSummary(aiResult.getCertificationsAwardsSummary());
            jobseekerAnalysis.setOtherActivitiesSummary(aiResult.getOtherActivitiesSummary());

            // Chuyển đổi List<String> thành String (phân tách bằng dấu phẩy) để lưu vào DB
            jobseekerAnalysis.setKeyTechnologiesTools(
                    aiResult.getKeyTechnologiesTools() != null ? String.join(", ", aiResult.getKeyTechnologiesTools()) : null);
            jobseekerAnalysis.setSoftSkillsIdentified(
                    aiResult.getSoftSkillsIdentified() != null ? String.join(", ", aiResult.getSoftSkillsIdentified()) : null);
            jobseekerAnalysis.setDomainExpertiseKeywords(
                    aiResult.getDomainExpertiseKeywords() != null ? String.join(", ", aiResult.getDomainExpertiseKeywords()) : null);
            jobseekerAnalysis.setRelevantEventKeywords(
                    aiResult.getRelevantEventKeywords() != null ? String.join(", ", aiResult.getRelevantEventKeywords()) : null);

            jobseekerAnalysis.setCareerLevelPrediction(aiResult.getCareerLevelPrediction());
            jobseekerAnalysis.setPotentialEventSuitability(aiResult.getPotentialEventSuitability());

            jobseekerAnalysis.setAiAnalysisVersion(geminiModelUsedForAnalysis);
            jobseekerAnalysis.setAnalysisTimestamp(LocalDateTime.now());
            jobseekerAnalysis.setRawGeminiResponse(jsonString); // Lưu JSON thô từ Gemini

            JobseekerAnalysis savedAnalysis = jobseekerAnalysisRepository.save(jobseekerAnalysis);
            log.info("Successfully saved/updated JobseekerAnalysis for UserDetail ID: {} with ID: {}", userDetail.getId(), savedAnalysis.getId());
            return savedAnalysis;

        } catch (AppException e) {
            log.error("Application-specific error during AI analysis for UserDetail ID {}: {}", userDetail.getId(), e.getMessage(), e);
            throw e; // Ném lại AppException đã xác định
        } catch (IOException e) {
            log.error("I/O error during AI analysis for UserDetail ID {}: {}", userDetail.getId(), e.getMessage(), e);
            throw new AppException(ErrorCode.AI_SERVICE_ERROR); // Lỗi giao tiếp AI hoặc đọc/ghi
        } catch (Exception e) {
            log.error("An unexpected error occurred during AI analysis for UserDetail ID {}: {}", userDetail.getId(), e.getMessage(), e);
            throw new AppException(ErrorCode.AI_SERVICE_ERROR); // Bắt các lỗi chung khác
        }
    }

    /**
     * Trích xuất nội dung văn bản từ một tệp PDF được cung cấp bởi URL.
     *
     * @param resumeUrl URL của tệp PDF resume.
     * @return Nội dung văn bản đã trích xuất từ PDF.
     * @throws AppException Nếu có lỗi trong quá trình đọc PDF.
     */
    private String extractPdfContent(String resumeUrl) throws AppException {
        String resumeContent = "";
        try {
            URL url = new URL(resumeUrl);
            PDDocument document = PDDocument.load(url.openStream());
            PDFTextStripper pdfStripper = new PDFTextStripper();
            resumeContent = pdfStripper.getText(document);
            document.close();
            log.info("Successfully extracted content from resume URL: {}", resumeUrl);
        } catch (IOException e) {
            log.error("Error reading resume content from URL {} using PDFBox: {}", resumeUrl, e.getMessage(), e);
            throw new AppException(ErrorCode.RESUME_PROCESSING_ERROR);
        } catch (Exception e) {
            log.error("An unexpected error occurred while processing resume URL {}: {}", resumeUrl, e.getMessage(), e);
            throw new AppException(ErrorCode.RESUME_PROCESSING_ERROR);
        }
        return resumeContent;
    }

    /**
     * Xây dựng chuỗi prompt chi tiết cho Gemini để phân tích resume.
     *
     * @param resumeContent Nội dung văn bản của resume.
     * @return Chuỗi prompt đã hoàn chỉnh.
     */
    private String buildJobseekerAnalysisPrompt(String resumeContent) {
        return """
            Bạn là một chuyên gia phân tích hồ sơ ứng viên chuyên sâu. Nhiệm vụ của bạn là phân tích chi tiết nội dung resume dưới đây và trả về kết quả dưới dạng JSON theo cấu trúc sau. Đảm bảo mọi thông tin đều được trích xuất trực tiếp từ resume và không suy diễn.
            
            ```json
            {
              "fullName": "<Tên đầy đủ của ứng viên>",
              "email": "<Email liên hệ>",
              "phoneNumber": "<Số điện thoại liên hệ>",
              "careerObjectiveSummary": "<Tóm tắt mục tiêu nghề nghiệp hoặc giới thiệu bản thân (nếu có), tối đa 2 câu>",
              "workExperienceSummary": "<Tóm tắt kinh nghiệm làm việc (liệt kê tối đa 3 vị trí gần đây nhất: tên công ty, chức danh, thời gian, 1-2 gạch đầu dòng về trách nhiệm/thành tựu nổi bật). Nếu không có kinh nghiệm chính thức, ghi rõ 'Không có kinh nghiệm làm việc chính thức được đề cập.'>",
              "skillsSummary": "<Tóm tắt kỹ năng (ngôn ngữ lập trình, frameworks, cơ sở dữ liệu, công cụ, kỹ năng mềm) dựa trên các kỹ năng được đề cập rõ ràng. Phân loại nếu có thể.>",
              "educationSummary": "<Tóm tắt học vấn (bằng cấp cao nhất, tên trường, thời gian tốt nghiệp/đang học)>",
              "projectsActivitiesSummary": "<Tóm tắt 1-2 dự án hoặc hoạt động nổi bật, nêu rõ vai trò và kết quả chính>",
              "certificationsAwardsSummary": "<Liệt kê các chứng chỉ và giải thưởng quan trọng (ví dụ: IELTS, chứng chỉ chuyên môn)>",
              "otherActivitiesSummary": "<Tóm tắt các hoạt động khác không phải kinh nghiệm/dự án chính (ví dụ: câu lạc bộ, tình nguyện, vị trí lãnh đạo)>",
              "keyTechnologiesTools": [<"Java">, <"Spring Boot">, <"ReactJS">, <"Docker">],
              "softSkillsIdentified": [<"Giao tiếp">, <"Làm việc nhóm">, <"Giải quyết vấn đề">, <"Lãnh đạo">],
              "domainExpertiseKeywords": [<"Web Development">, <"Mobile Development">, <"AI/ML">, <"Data Science">, <"Embedded Systems">],
              "careerLevelPrediction": "<Dự đoán cấp độ nghề nghiệp: Fresher, Junior, Mid, Senior, Lead, hoặc Unclear nếu không đủ thông tin>",
              "potentialEventSuitability": "<Đánh giá tổng quan (1-2 câu) về sự phù hợp của ứng viên cho các loại sự kiện (ví dụ: 'Phù hợp cho tuần lễ tuyển dụng sinh viên IT', 'Có tiềm năng cho Hackathon AI', 'Quan tâm đến cơ hội khởi nghiệp bền vững').>",
              "relevantEventKeywords": [<"Tuyển dụng lập trình viên">, <"Cơ hội thực tập">, <"Hackathon AI">, <"Phát triển bền vững">, <"Data Analytics">, <"Khởi nghiệp">]
            }
            ```
            
            Nếu một phần thông tin không có trong resume, hãy để trống chuỗi ("") hoặc danh sách rỗng ([]) cho các trường tương ứng. Đối với các trường tóm tắt (ví dụ: workExperienceSummary), nếu không có dữ liệu, hãy ghi rõ "Không có...".
            
            --- Bắt đầu Resume ---
            """ + resumeContent + """
            --- Kết thúc Resume ---
            """;
    }

    /**
     * Trích xuất chuỗi JSON từ một chuỗi văn bản lớn hơn (ví dụ: phản hồi từ Gemini).
     * Tìm kiếm khối JSON được bao quanh bởi ```json hoặc một đối tượng JSON độc lập.
     *
     * @param text Chuỗi văn bản chứa JSON.
     * @return Chuỗi JSON đã trích xuất, hoặc chuỗi rỗng nếu không tìm thấy.
     */
    private String extractJsonFromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Regex để tìm khối JSON bao quanh bởi ```json...``` HOẶC một đối tượng JSON độc lập {...}
        Pattern pattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```|(\\{[\\s\\S]*\\})", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String jsonContent = matcher.group(1); // Lấy nội dung trong ```json...```
            if (jsonContent != null && !jsonContent.trim().isEmpty()) {
                log.debug("Extracted JSON from code block.");
                return jsonContent.trim();
            } else {
                jsonContent = matcher.group(2); // Lấy nội dung là một đối tượng JSON độc lập
                if (jsonContent != null && !jsonContent.trim().isEmpty()) {
                    log.debug("Extracted standalone JSON object.");
                    return jsonContent.trim();
                }
            }
        }
        log.warn("Could not find a valid JSON structure in the Gemini response text.");
        return "";
    }
}