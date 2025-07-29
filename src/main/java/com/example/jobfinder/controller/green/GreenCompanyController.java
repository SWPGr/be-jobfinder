//package com.example.jobfinder.controller.green;
//
//import com.example.jobfinder.dto.green.GreenCompanyAnalysis;
//import com.example.jobfinder.dto.job.JobRecommendationResponse;
//import com.example.jobfinder.service.GreenCompanyDetectionService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//@RestController
//@RequestMapping("/api/green-test")
//@RequiredArgsConstructor
//@Slf4j
//public class GreenCompanyController {
//
//    private final GreenCompanyDetectionService greenCompanyDetectionService;
//
//    @GetMapping("/analyze/{employerId}")
//    public ResponseEntity<Map<String, Object>> analyzeCompany(@PathVariable Long employerId) {
//        log.info("Testing green analysis for employer: {}", employerId);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("timestamp", LocalDateTime.now());
//        response.put("employerId", employerId);
//
//        try {
//            GreenCompanyAnalysis analysis = greenCompanyDetectionService.analyzeGreenCompany(employerId);
//
//            response.put("success", true);
//            response.put("analysis", analysis);
//
//            // Thêm summary để dễ đọc
//            Map<String, Object> summary = new HashMap<>();
//            summary.put("isGreen", analysis.isGreenCompany());
//            summary.put("score", analysis.getOverallGreenScore());
//            summary.put("level", analysis.getCertificationLevel());
//            summary.put("keywordCount", analysis.getGreenKeywords() != null ? analysis.getGreenKeywords().size() : 0);
//
//            response.put("summary", summary);
//
//        } catch (Exception e) {
//            log.error("Error testing green analysis", e);
//            response.put("success", false);
//            response.put("error", e.getMessage());
//        }
//
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * 2. Test quick check - Chỉ return true/false
//     * GET /api/green-test/quick/{employerId}
//     */
//    @GetMapping("/quick/{employerId}")
//    public ResponseEntity<Map<String, Object>> quickCheck(@PathVariable Long employerId) {
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            GreenCompanyAnalysis analysis = greenCompanyDetectionService.analyzeGreenCompany(employerId);
//
//            response.put("employerId", employerId);
//            response.put("isGreen", analysis.isGreenCompany());
//            response.put("score", Math.round(analysis.getOverallGreenScore() * 100.0) / 100.0);
//            response.put("status", "success");
//
//        } catch (Exception e) {
//            response.put("employerId", employerId);
//            response.put("isGreen", false);
//            response.put("score", 0.0);
//            response.put("status", "error");
//            response.put("message", e.getMessage());
//        }
//
//        return ResponseEntity.ok(response);
//    }
//}