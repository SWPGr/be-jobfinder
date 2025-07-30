package com.example.jobfinder.service;

import com.example.jobfinder.dto.ai.CompanyAnalysisAIDTO;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.BaseNameEntity;
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
    private final GeminiService geminiService;

    @Value("${google.gemini.model-name}")
    private String geminiModelUsedForAnalysis;

    private String buildCompanyAnalysisPrompt(UserDetail userDetail) {

        String prompt = "Bạn là một chuyên gia phân tích doanh nghiệp và hợp tác chiến lược. Nhiệm vụ của bạn là phân tích sâu hồ sơ công ty và cung cấp các thông tin chi tiết có cấu trúc JSON, phục vụ cho việc đánh giá tiềm năng hợp tác trong các sự kiện đa dạng (ví dụ: tuyển dụng, công nghệ, cộng đồng, bền vững).\n" +
                "Dựa trên thông tin hồ sơ công ty sau, hãy phân tích và trả về kết quả bằng tiếng anh dưới dạng JSON theo cấu trúc sau:\n" +
                "```json\n" +
                "{\n" +
                "  \"industryKeywords\": \"<các từ khóa ngành nghề chính, phân tách bằng dấu phẩy, ví dụ: Software Development, AI/ML, Fintech>\",\n" +
                "  \"coreCompetencies\": \"<các năng lực cốt lõi của công ty, phân tách bằng dấu phẩy, ví dụ: Backend Development, Mobile Apps, Cloud Solutions, Data Analytics>\",\n" +
                "  \"cultureDescription\": \"<mô tả ngắn gọn (1-2 câu) về văn hóa làm việc và môi trường công ty>\",\n" +
                "  \"targetCandidateProfile\": \"<mô tả ngắn gọn (1-2 câu) về hồ sơ ứng viên lý tưởng mà công ty tìm kiếm>\",\n" +
                "  \"growthPotentialSummary\": \"<tóm tắt ngắn gọn (1-2 câu) tiềm năng phát triển, mở rộng thị trường hoặc đổi mới của công ty>\",\n" +
                "  \"marketPositioningSummary\": \"<tóm tắt ngắn gọn (1-2 câu) vị thế cạnh tranh và sự khác biệt của công ty trên thị trường>\",\n" +
                "  \"csrAndSustainabilityInitiatives\": \"<mô tả các sáng kiến, cam kết về trách nhiệm xã hội, môi trường, và quản trị (ESG) của công ty>\",\n" +
                "  \"talentDevelopmentFocus\": \"<mô tả mức độ tập trung vào đào tạo, phát triển kỹ năng, và lộ trình sự nghiệp cho nhân viên>\",\n" +
                "  \"talentEngagementPrograms\": \"<liệt kê các chương trình thu hút và gắn kết tài năng, bao gồm thực tập, mentorship, fresher, hay các chương trình hợp tác với trường đại học>\",\n" +
                "  \"communityOutreachPrograms\": \"<mô tả các hoạt động và cam kết liên quan đến việc đóng góp cho cộng đồng địa phương hoặc xã hội rộng lớn hơn>\",\n" +
                "  \"eventCollaborationPotential\": \"<đánh giá tổng quan (1-2 câu) về tiềm năng hợp tác của công ty cho các loại sự kiện khác nhau (ví dụ: tuyển dụng, công nghệ, xã hội, môi trường)>\",\n" +
                "  \"relevantPartnershipKeywords\": \"<các từ khóa cụ thể liên quan đến tiềm năng hợp tác/sự kiện (ví dụ: 'tech conference', 'career fair', 'environmental workshop', 'community service'), phân tách bằng dấu phẩy>\"\n" +
                "}\n" +
                "```\n\n" +
                "Nếu một trường thông tin không có sẵn hoặc không liên quan, hãy để trống chuỗi hoặc dùng giá trị null.\n" +
                "Hồ sơ công ty:\n" +
                "-------------------------------------------\n" +
                "Tên công ty: " + Optional.ofNullable(userDetail.getCompanyName()).orElse("Chưa cung cấp") + "\n" +
                "Mô tả: " + Optional.ofNullable(userDetail.getDescription()).orElse("Chưa cung cấp") + "\n" +
                "Website: " + Optional.ofNullable(userDetail.getWebsite()).orElse("Chưa cung cấp") + "\n" +
                "Quy mô đội ngũ: " + Optional.ofNullable(userDetail.getTeamSize()).orElse("Chưa cung cấp") + "\n" +
                "Năm thành lập: " + Optional.ofNullable(userDetail.getYearOfEstablishment()).map(String::valueOf).orElse("Chưa cung cấp") + "\n" +
                "Địa điểm: " + Optional.ofNullable(userDetail.getLocation()).orElse("Chưa cung cấp") + "\n" +
                "Tầm nhìn công ty: " + Optional.ofNullable(userDetail.getCompanyVision()).orElse("Chưa cung cấp") + "\n" +
                "Loại hình tổ chức: " + Optional.ofNullable(userDetail.getOrganization()).map(BaseNameEntity::getName).orElse("Chưa cung cấp") + "\n" +
                "Ngành nghề chính: " + Optional.ofNullable(userDetail.getCategory()).map(BaseNameEntity::getName).orElse("Chưa cung cấp") + "\n" +
                "-------------------------------------------\n" +
                "Chỉ trả về JSON. KHÔNG THÊM BẤT KỲ VĂN BẢN GIỚI THIỆU HOẶC GIẢI THÍCH NÀO NGOÀI JSON.\n";

        return prompt;
    }

    public void analyzeAndSaveCompanyProfile(UserDetail userDetail) {
        if (userDetail == null || userDetail.getId() == null) {
            throw new AppException(ErrorCode.USERNAME_INVALID);
        }

        String prompt = buildCompanyAnalysisPrompt(userDetail);
        String rawGeminiResponseText;

        try {
            rawGeminiResponseText = geminiService.getGeminiResponse(prompt);
            log.info("Raw text response from Gemini for UserDetail {}: {}", userDetail.getId(), rawGeminiResponseText);

            String jsonString = extractJsonFromString(rawGeminiResponseText);
            if (jsonString.isEmpty()) {
                log.error("No valid JSON found in Gemini response for UserDetail {}: {}", userDetail.getId(), rawGeminiResponseText);
                throw new AppException(ErrorCode.AI_PARSE_ERROR);
            }

            CompanyAnalysisAIDTO aiResult = objectMapper.readValue(jsonString, CompanyAnalysisAIDTO.class);

            Optional<CompanyAnalysis> existingAnalysis = companyAnalysisRepository.findByUserDetail(userDetail);
            CompanyAnalysis companyAnalysis = existingAnalysis.orElseGet(CompanyAnalysis::new);
            companyAnalysis.setUserDetail(userDetail);
            companyAnalysis.setIndustryKeywords(aiResult.getIndustryKeywords());
            companyAnalysis.setCoreCompetencies(aiResult.getCoreCompetencies());
            companyAnalysis.setCultureDescription(aiResult.getCultureDescription());
            companyAnalysis.setTargetCandidateProfile(aiResult.getTargetCandidateProfile());
            companyAnalysis.setGrowthPotentialSummary(aiResult.getGrowthPotentialSummary());
            companyAnalysis.setMarketPositioningSummary(aiResult.getMarketPositioningSummary());
            companyAnalysis.setCsrAndSustainabilityInitiatives(aiResult.getCsrAndSustainabilityInitiatives());
            companyAnalysis.setTalentDevelopmentFocus(aiResult.getTalentDevelopmentFocus());
            companyAnalysis.setTalentEngagementPrograms(aiResult.getTalentEngagementPrograms());
            companyAnalysis.setCommunityOutreachPrograms(aiResult.getCommunityOutreachPrograms());
            companyAnalysis.setEventCollaborationPotential(aiResult.getEventCollaborationPotential());
            companyAnalysis.setRelevantPartnershipKeywords(aiResult.getRelevantPartnershipKeywords());
            companyAnalysis.setAiAnalysisVersion(geminiModelUsedForAnalysis);
            companyAnalysis.setAnalysisTimestamp(LocalDateTime.now());
            companyAnalysis.setRawAiResponse(jsonString);
            companyAnalysisRepository.save(companyAnalysis);
        } catch (IOException e) {
            log.error("Error processing AI response or IO issue for UserDetail {}: {}", userDetail.getId(), e.getMessage(), e);
            throw new AppException(ErrorCode.AI_SERVICE_ERROR);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred during AI analysis for UserDetail {}: {}", userDetail.getId(), e.getMessage(), e);
            throw new AppException(ErrorCode.AI_SERVICE_ERROR);
        }
    }

    private String extractJsonFromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        Pattern pattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```|(\\{[\\s\\S]*})", Pattern.DOTALL);
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
        return "";
    }
}