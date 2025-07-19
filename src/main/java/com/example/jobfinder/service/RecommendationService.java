//package com.example.jobfinder.service;
//
//import com.example.jobfinder.dto.green.GreenCompanyAnalysis;
//import com.example.jobfinder.dto.green.GreenJobAnalysis;
//import com.example.jobfinder.dto.job.JobRecommendationResponse;
//import com.example.jobfinder.exception.AppException;
//import com.example.jobfinder.exception.ErrorCode;
//import com.example.jobfinder.model.*;
//import com.example.jobfinder.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class RecommendationService {
//    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
//
//    private final UserRepository userRepository;
//    private final JobRepository jobRepository;
//    private final UserDetailsRepository userDetailsRepository;
//    private final JobRecommendationRepository jobRecommendationRepository;
//    private final SearchHistoryRepository searchHistoryRepository;
//    // ...existing imports...
//    private final GreenCompanyDetectionService greenCompanyDetectionService; // Thay đổi dependency
//
//    /**
//     * Generate green recommendations cho specific user - ONLY GREEN COMPANIES
//     */
//    @Transactional
//    public void generateGreenRecommendationsForUser(User jobSeeker) {
//        log.debug("Generating green recommendations for user: {}", jobSeeker.getEmail());
//
//        try {
//            UserDetail userDetail = userDetailsRepository.findByUserId(jobSeeker.getId())
//                    .orElse(null);
//
//            if (userDetail == null) {
//                log.warn("User detail not found for user: {}", jobSeeker.getEmail());
//                return;
//            }
//
//            // Analyze user profile cho green interests (simplified)
//            GreenJobAnalysis userGreenAnalysis = analyzeUserGreenProfile(userDetail, jobSeeker);
//
//
//            // Get all active jobs
//            List<Job> activeJobs = jobRepository.findByActiveTrue();
//
//            // Analyze và score jobs - CHỈ TỪ GREEN COMPANIES
//            List<JobRecommendation> greenRecommendations = new ArrayList<>();
//
//            for (Job job : activeJobs) {
//                try {
//                    Long employerId = getEmployerId(job);
//
//                    if (employerId != null) {
//                        // *** MAIN CHANGE: Check if company is green FIRST ***
//                        GreenCompanyAnalysis companyAnalysis = greenCompanyDetectionService.analyzeGreenCompany(employerId);
//
//                        // CHỈ RECOMMEND JOBS TỪ GREEN COMPANIES
//                        if (companyAnalysis.isGreenCompany()) {
//                            float greenScore = calculateGreenJobScoreWithCompany(userDetail, job,
//                                    userGreenAnalysis, companyAnalysis);
//
//                            if (greenScore >= 0.4) { // Threshold cho green jobs
//                                JobRecommendation recommendation = new JobRecommendation();
//                                recommendation.setJobSeeker(jobSeeker);
//                                recommendation.setJob(job);
//                                recommendation.setScore(greenScore);
//                                recommendation.setRecommendedAt(LocalDateTime.now());
//                                greenRecommendations.add(recommendation);
//                            }
//                        }
//                        // SKIP nếu không phải green company
//                    }
//                } catch (Exception e) {
//                    log.error("Error analyzing job {} for user {}", job.getId(), jobSeeker.getEmail(), e);
//                }
//            }
//
//            // Sort và limit recommendations
//            List<JobRecommendation> topGreenRecommendations = greenRecommendations.stream()
//                    .sorted(Comparator.comparing(JobRecommendation::getScore).reversed())
//                    .limit(15) // Green job recommendations
//                    .collect(Collectors.toList());
//
//            // Clear existing recommendations và save new ones
//            clearExistingRecommendations(jobSeeker.getId());
//            if (!topGreenRecommendations.isEmpty()) {
//                jobRecommendationRepository.saveAll(topGreenRecommendations);
//            }
//
//            log.debug("Saved {} green recommendations from green companies for user: {}",
//                    topGreenRecommendations.size(), jobSeeker.getEmail());
//
//        } catch (Exception e) {
//            log.error("Error generating recommendations for user: {}", jobSeeker.getEmail(), e);
//        }
//    }
//
//    /**
//     * NEW: Analyze user green profile (simplified without heavy NLP)
//     */
//    private GreenJobAnalysis analyzeUserGreenProfile(UserDetail userDetail, User user) {
//        String userProfile = buildUserProfile(userDetail, user);
//
//        // Simple green keyword detection
//        List<String> detectedKeywords = extractGreenKeywords(userProfile);
//        List<String> detectedCategories = detectGreenCategories(userProfile);
//        double greenScore = calculateUserGreenScore(detectedKeywords, detectedCategories);
//
//        return new GreenJobAnalysis(
//                greenScore > 0.3,
//                greenScore,
//                detectedKeywords,
//                detectedCategories
//        );
//    }
//}