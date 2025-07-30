package com.example.jobfinder.service;

import com.example.jobfinder.dto.ai.JobseekerAnalysisAIDTO;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.JobseekerAnalysis;
import com.example.jobfinder.model.UserDetail;
import com.example.jobfinder.repository.JobseekerAnalysisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobseekerAnalysisService {
    final GeminiService geminiService;
    final ObjectMapper objectMapper;
    final JobseekerAnalysisRepository jobseekerAnalysisRepository;

    @Value("${google.gemini.model-name}")
    String geminiModelUsedForAnalysis;

    @Transactional
    public void analyzeAndSaveJobseekerResume(UserDetail userDetail) throws IOException {
        log.info("Starting AI analysis for Jobseeker resume. UserDetail ID: {}", userDetail.getId());

        String resumeUrl = userDetail.getResumeUrl();
        if (resumeUrl == null || resumeUrl.trim().isEmpty()) {
            log.warn("Resume URL not found for UserDetail ID: {}", userDetail.getId());
            throw new AppException(ErrorCode.RESUME_NOT_FOUND_FOR_APPLICATION);
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
            } else {
                jobseekerAnalysis = new JobseekerAnalysis();
                jobseekerAnalysis.setUserDetail(userDetail);
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
            jobseekerAnalysis.setRawGeminiResponse(jsonString);

            JobseekerAnalysis savedAnalysis = jobseekerAnalysisRepository.save(jobseekerAnalysis);
            log.info("Successfully saved/updated JobseekerAnalysis for UserDetail ID: {} with ID: {}", userDetail.getId(), savedAnalysis.getId());

        } catch (AppException e) {
            log.error("Application-specific error during AI analysis for UserDetail ID {}: {}", userDetail.getId(), e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            log.error("I/O error during AI analysis for UserDetail ID {}: {}", userDetail.getId(), e.getMessage(), e);
            throw new AppException(ErrorCode.AI_SERVICE_ERROR);
        } catch (Exception e) {
            log.error("An unexpected error occurred during AI analysis for UserDetail ID {}: {}", userDetail.getId(), e.getMessage(), e);
            throw new AppException(ErrorCode.AI_SERVICE_ERROR);
        }
    }

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

    private String extractJsonFromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        Pattern pattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```|(\\{[\\s\\S]*\\})", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String jsonContent = matcher.group(1);
            if (jsonContent != null && !jsonContent.trim().isEmpty()) {
                log.debug("Extracted JSON from code block.");
                return jsonContent.trim();
            } else {
                jsonContent = matcher.group(2);
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