package com.example.jobfinder.service;

import com.example.jobfinder.dto.ai.CompanyAnalysisAIDTO;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.CompanyAnalysis;
import com.example.jobfinder.model.UserDetail;
import com.example.jobfinder.repository.CompanyAnalysisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; // Có thể giữ lại nếu bạn muốn versioning bằng model name
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AICompanyAnalysisService {

    private final ObjectMapper objectMapper;
    private final CompanyAnalysisRepository companyAnalysisRepository;
    private final GeminiService geminiService; // <-- GIỮ NGUYÊN INJECT GeminiService

    // Có thể lấy model name từ GeminiService hoặc giữ riêng nếu muốn có phiên bản đặc thù
    @Value("${google.gemini.model-name}") // Lấy model name từ cấu hình chung của GeminiService
    private String geminiModelUsedForAnalysis; // Dùng để lưu vào DB

    /**
     * Tạo prompt cho Gemini từ thông tin UserDetail của công ty.
     * @param userDetail Thông tin hồ sơ công ty.
     * @return Chuỗi prompt đã được định dạng.
     */
    private String buildCompanyAnalysisPrompt(UserDetail userDetail) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là một chuyên gia phân tích doanh nghiệp và hợp tác chiến lược. Nhiệm vụ của bạn là phân tích sâu hồ sơ công ty và cung cấp các thông tin chi tiết có cấu trúc JSON, phục vụ cho việc đánh giá tiềm năng hợp tác trong các sự kiện đa dạng (ví dụ: tuyển dụng, công nghệ, cộng đồng, bền vững).\n");
        prompt.append("Dựa trên thông tin hồ sơ công ty sau, hãy phân tích và trả về kết quả dưới dạng JSON theo cấu trúc sau:\n");
        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"industryKeywords\": \"<các từ khóa ngành nghề chính, phân tách bằng dấu phẩy, ví dụ: Software Development, AI/ML, Fintech>\",\n");
        prompt.append("  \"coreCompetencies\": \"<các năng lực cốt lõi của công ty, phân tách bằng dấu phẩy, ví dụ: Backend Development, Mobile Apps, Cloud Solutions, Data Analytics>\",\n");
        prompt.append("  \"cultureDescription\": \"<mô tả ngắn gọn (1-2 câu) về văn hóa làm việc và môi trường công ty>\",\n");
        prompt.append("  \"targetCandidateProfile\": \"<mô tả ngắn gọn (1-2 câu) về hồ sơ ứng viên lý tưởng mà công ty tìm kiếm>\",\n");
        prompt.append("  \"growthPotentialSummary\": \"<tóm tắt ngắn gọn (1-2 câu) tiềm năng phát triển, mở rộng thị trường hoặc đổi mới của công ty>\",\n");
        prompt.append("  \"marketPositioningSummary\": \"<tóm tắt ngắn gọn (1-2 câu) vị thế cạnh tranh và sự khác biệt của công ty trên thị trường>\",\n");
        prompt.append("  \"csrAndSustainabilityInitiatives\": \"<mô tả các sáng kiến, cam kết về trách nhiệm xã hội, môi trường, và quản trị (ESG) của công ty>\",\n");
        prompt.append("  \"talentDevelopmentFocus\": \"<mô tả mức độ tập trung vào đào tạo, phát triển kỹ năng, và lộ trình sự nghiệp cho nhân viên>\",\n");
        prompt.append("  \"talentEngagementPrograms\": \"<liệt kê các chương trình thu hút và gắn kết tài năng, bao gồm thực tập, mentorship, fresher, hay các chương trình hợp tác với trường đại học>\",\n");
        prompt.append("  \"communityOutreachPrograms\": \"<mô tả các hoạt động và cam kết liên quan đến việc đóng góp cho cộng đồng địa phương hoặc xã hội rộng lớn hơn>\",\n");
        prompt.append("  \"eventCollaborationPotential\": \"<đánh giá tổng quan (1-2 câu) về tiềm năng hợp tác của công ty cho các loại sự kiện khác nhau (ví dụ: tuyển dụng, công nghệ, xã hội, môi trường)>\",\n");
        prompt.append("  \"relevantPartnershipKeywords\": \"<các từ khóa cụ thể liên quan đến tiềm năng hợp tác/sự kiện (ví dụ: 'tech conference', 'career fair', 'environmental workshop', 'community service'), phân tách bằng dấu phẩy>\"\n");
        prompt.append("}\n");
        prompt.append("```\n\n");

        prompt.append("Nếu một trường thông tin không có sẵn hoặc không liên quan, hãy để trống chuỗi hoặc dùng giá trị null.\n");
        prompt.append("Hồ sơ công ty:\n");
        prompt.append("-------------------------------------------\n");
        prompt.append("Tên công ty: ").append(Optional.ofNullable(userDetail.getCompanyName()).orElse("Chưa cung cấp")).append("\n");
        prompt.append("Mô tả: ").append(Optional.ofNullable(userDetail.getDescription()).orElse("Chưa cung cấp")).append("\n");
        prompt.append("Website: ").append(Optional.ofNullable(userDetail.getWebsite()).orElse("Chưa cung cấp")).append("\n");
        prompt.append("Quy mô đội ngũ: ").append(Optional.ofNullable(userDetail.getTeamSize()).orElse("Chưa cung cấp")).append("\n");
        prompt.append("Năm thành lập: ").append(Optional.ofNullable(userDetail.getYearOfEstablishment()).map(String::valueOf).orElse("Chưa cung cấp")).append("\n");
        prompt.append("Địa điểm: ").append(Optional.ofNullable(userDetail.getLocation()).orElse("Chưa cung cấp")).append("\n");
        prompt.append("Tầm nhìn công ty: ").append(Optional.ofNullable(userDetail.getCompanyVision()).orElse("Chưa cung cấp")).append("\n");
        prompt.append("Loại hình tổ chức: ").append(Optional.ofNullable(userDetail.getOrganization()).map(o -> o.getName()).orElse("Chưa cung cấp")).append("\n");
        prompt.append("Ngành nghề chính: ").append(Optional.ofNullable(userDetail.getCategory()).map(c -> c.getName()).orElse("Chưa cung cấp")).append("\n");
        prompt.append("-------------------------------------------\n");
        prompt.append("Chỉ trả về JSON. KHÔNG THÊM BẤT KỲ VĂN BẢN GIỚI THIỆU HOẶC GIẢI THÍCH NÀO NGOÀI JSON.\n");

        return prompt.toString();
    }

    /**
     * Phân tích hồ sơ công ty bằng AI (sử dụng GeminiService) và lưu vào database.
     * @param userDetail Hồ sơ công ty (UserDetail entity) cần phân tích.
     * @return CompanyAnalysis entity đã được lưu.
     */
    public CompanyAnalysis analyzeAndSaveCompanyProfile(UserDetail userDetail) {
        if (userDetail == null || userDetail.getId() == null) {
            throw new AppException(ErrorCode.USERNAME_INVALID);
        }

        String prompt = buildCompanyAnalysisPrompt(userDetail);
        String rawGeminiResponseText; // Sẽ chứa chuỗi văn bản từ Gemini

        try {
            // Sử dụng hàm getGeminiResponse có sẵn của bạn
            rawGeminiResponseText = geminiService.getGeminiResponse(prompt);
            log.info("Raw text response from Gemini for UserDetail {}: {}", userDetail.getId(), rawGeminiResponseText);

            // Xử lý chuỗi rawGeminiResponseText để trích xuất JSON
            String jsonString = extractJsonFromString(rawGeminiResponseText);
            if (jsonString.isEmpty()) {
                log.error("No valid JSON found in Gemini response for UserDetail {}: {}", userDetail.getId(), rawGeminiResponseText);
                throw new AppException(ErrorCode.AI_PARSE_ERROR);
            }

            // Parse JSON response từ AI thành DTO
            CompanyAnalysisAIDTO aiResult = objectMapper.readValue(jsonString, CompanyAnalysisAIDTO.class);

            // Tìm bản ghi phân tích cũ nếu có, hoặc tạo mới
            Optional<CompanyAnalysis> existingAnalysis = companyAnalysisRepository.findByUserDetail(userDetail);
            CompanyAnalysis companyAnalysis = existingAnalysis.orElseGet(CompanyAnalysis::new);

            // Cập nhật các trường từ kết quả AI
            companyAnalysis.setUserDetail(userDetail);
            companyAnalysis.setIndustryKeywords(aiResult.getIndustryKeywords());
            companyAnalysis.setCoreCompetencies(aiResult.getCoreCompetencies());
            companyAnalysis.setCultureDescription(aiResult.getCultureDescription());
            companyAnalysis.setTargetCandidateProfile(aiResult.getTargetCandidateProfile());
            companyAnalysis.setGrowthPotentialSummary(aiResult.getGrowthPotentialSummary());
            companyAnalysis.setMarketPositioningSummary(aiResult.getMarketPositioningSummary());

            // CẬP NHẬT CÁC TRƯỜNG MỚI
            companyAnalysis.setCsrAndSustainabilityInitiatives(aiResult.getCsrAndSustainabilityInitiatives());
            companyAnalysis.setTalentDevelopmentFocus(aiResult.getTalentDevelopmentFocus());
            companyAnalysis.setTalentEngagementPrograms(aiResult.getTalentEngagementPrograms());
            companyAnalysis.setCommunityOutreachPrograms(aiResult.getCommunityOutreachPrograms());
            companyAnalysis.setEventCollaborationPotential(aiResult.getEventCollaborationPotential());
            companyAnalysis.setRelevantPartnershipKeywords(aiResult.getRelevantPartnershipKeywords());

            companyAnalysis.setAiAnalysisVersion(geminiModelUsedForAnalysis);
            companyAnalysis.setAnalysisTimestamp(LocalDateTime.now());
            companyAnalysis.setRawAiResponse(jsonString);


            return companyAnalysisRepository.save(companyAnalysis);

        } catch (IOException e) {
            log.error("Error processing AI response or IO issue for UserDetail {}: {}", userDetail.getId(), e.getMessage(), e);
            throw new AppException(ErrorCode.AI_SERVICE_ERROR);
        } catch (AppException e) {
            // Re-throw AppException if it's already an App Error (e.g., from parseJsonFromString)
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred during AI analysis for UserDetail {}: {}", userDetail.getId(), e.getMessage(), e);
            throw new AppException(ErrorCode.AI_SERVICE_ERROR);
        }
    }

    /**
     * Phương thức helper để trích xuất chuỗi JSON từ phản hồi văn bản của Gemini.
     * Gemini thường bao bọc JSON trong ```json ... ``` hoặc đôi khi chỉ trả về JSON thuần.
     * @param text Chuỗi văn bản nhận được từ Gemini.
     * @return Chuỗi JSON đã được trích xuất, hoặc chuỗi rỗng nếu không tìm thấy.
     */
    private String extractJsonFromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Regex để tìm nội dung JSON bên trong ```json ... ``` hoặc { ... }
        // Pattern.DOTALL cho phép '.' khớp với cả ký tự xuống dòng
        Pattern pattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```|(\\{[\\s\\S]*\\})", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String jsonContent = matcher.group(1);
            if (jsonContent != null) {
                return jsonContent.trim();
            } else if (matcher.group(2) != null) {
                return matcher.group(2).trim();
            }
        }
        log.warn("Could not find a JSON structure in the Gemini response text: {}", text);
        return ""; // Trả về chuỗi rỗng nếu không tìm thấy JSON
    }
}