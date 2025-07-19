package com.example.jobfinder.service;

import com.example.jobfinder.dto.green.GreenJobAnalysis;
import com.example.jobfinder.dto.job.JobRecommendationResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.*;
import com.example.jobfinder.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final JobRecommendationRepository jobRecommendationRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final GreenJobDetectionService greenJobDetectionService;
    // Remove NLPService dependency for now

    /**
     * Generate green job recommendations cho user
     */
    @Transactional
    public void generateGreenJobRecommendations() {
        log.debug("Generating green job recommendations for all JOB_SEEKER users");

        List<User> jobSeekers = userRepository.findAll().stream()
                .filter(user -> user.getRole() != null && "JOB_SEEKER".equalsIgnoreCase(user.getRole().getName()))
                .collect(Collectors.toList());

        for (User jobSeeker : jobSeekers) {
            try {
                generateGreenRecommendationsForUser(jobSeeker);
            } catch (Exception e) {
                log.error("Failed to generate green recommendations for user: {}", jobSeeker.getEmail(), e);
            }
        }
    }

    /**
     * Generate green recommendations cho specific user
     */
    @Transactional
    public void generateGreenRecommendationsForUser(User jobSeeker) {
        log.debug("Generating green recommendations for user: {}", jobSeeker.getEmail());

        try {
            UserDetail userDetail = userDetailsRepository.findByUserId(jobSeeker.getId())
                    .orElse(null);

            if (userDetail == null) {
                log.warn("User detail not found for user: {}", jobSeeker.getEmail());
                return;
            }

            // Analyze user profile cho green interests
            String userProfile = buildUserProfile(userDetail, jobSeeker);
            GreenJobAnalysis userGreenAnalysis = greenJobDetectionService.analyzeGreenJob(
                    null, userProfile, null);

            // Get user search history để hiểu interests
            List<String> searchHistory = getRecentSearchHistory(jobSeeker);

            // Get all active jobs
            List<Job> activeJobs = jobRepository.findByActiveTrue();

            // Analyze và score green jobs
            List<JobRecommendation> greenRecommendations = new ArrayList<>();

            for (Job job : activeJobs) {
                try {
                    String companyName = getCompanyName(job);
                    GreenJobAnalysis jobGreenAnalysis = greenJobDetectionService.analyzeGreenJob(
                            job.getTitle(), job.getDescription(), companyName);

                    if (jobGreenAnalysis.isGreenJob()) {
                        float greenScore = calculateGreenJobScore(userDetail, job, userGreenAnalysis,
                                jobGreenAnalysis, searchHistory);

                        if (greenScore >= 0.4) { // Higher threshold cho green jobs
                            JobRecommendation recommendation = new JobRecommendation();
                            recommendation.setJobSeeker(jobSeeker);
                            recommendation.setJob(job);
                            recommendation.setScore(greenScore);
                            recommendation.setRecommendedAt(LocalDateTime.now());
                            greenRecommendations.add(recommendation);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error analyzing job {} for user {}", job.getId(), jobSeeker.getEmail(), e);
                }
            }

            // Sort và limit recommendations
            List<JobRecommendation> topGreenRecommendations = greenRecommendations.stream()
                    .sorted(Comparator.comparing(JobRecommendation::getScore).reversed())
                    .limit(15) // More green job recommendations
                    .collect(Collectors.toList());

            // Clear existing recommendations và save new ones
            clearExistingRecommendations(jobSeeker.getId());
            if (!topGreenRecommendations.isEmpty()) {
                jobRecommendationRepository.saveAll(topGreenRecommendations);
            }

            log.debug("Saved {} green recommendations for user: {}",
                    topGreenRecommendations.size(), jobSeeker.getEmail());

        } catch (Exception e) {
            log.error("Error generating recommendations for user: {}", jobSeeker.getEmail(), e);
        }
    }

    /**
     * Calculate green job score
     */
    private float calculateGreenJobScore(UserDetail userDetail, Job job,
                                         GreenJobAnalysis userGreenAnalysis,
                                         GreenJobAnalysis jobGreenAnalysis,
                                         List<String> searchHistory) {
        float score = 0.0f;

        // Base green alignment score (30%)
        float greenAlignmentScore = calculateGreenAlignmentScore(userGreenAnalysis, jobGreenAnalysis);
        score += 0.30f * greenAlignmentScore;

        // Job green score bonus (25%)
        score += 0.25f * (float) jobGreenAnalysis.getGreenScore();

        // Traditional factors (35%) - reduced because we removed NLP similarity
        float traditionalScore = calculateTraditionalJobScore(userDetail, job);
        score += 0.35f * traditionalScore;

        // Search history green keywords bonus (10%)
        float searchHistoryScore = calculateSearchHistoryGreenScore(searchHistory, jobGreenAnalysis);
        score += 0.10f * searchHistoryScore;

        return Math.min(score, 1.0f);
    }

    /**
     * Calculate green alignment giữa user và job
     */
    private float calculateGreenAlignmentScore(GreenJobAnalysis userAnalysis, GreenJobAnalysis jobAnalysis) {
        if (userAnalysis.getGreenCategories() == null || userAnalysis.getGreenCategories().isEmpty()) {
            return 0.3f; // Base score nếu user chưa có green profile
        }

        // Calculate category overlap
        Set<String> userCategories = new HashSet<>(userAnalysis.getGreenCategories());
        Set<String> jobCategories = new HashSet<>(jobAnalysis.getGreenCategories());

        Set<String> intersection = new HashSet<>(userCategories);
        intersection.retainAll(jobCategories);

        float categoryAlignment = jobCategories.isEmpty() ? 0.0f :
                (float) intersection.size() / jobCategories.size();

        // Calculate keyword overlap
        Set<String> userKeywords = new HashSet<>(userAnalysis.getDetectedKeywords());
        Set<String> jobKeywords = new HashSet<>(jobAnalysis.getDetectedKeywords());

        Set<String> keywordIntersection = new HashSet<>(userKeywords);
        keywordIntersection.retainAll(jobKeywords);

        float keywordAlignment = jobKeywords.isEmpty() ? 0.0f :
                (float) keywordIntersection.size() / jobKeywords.size();

        return (categoryAlignment * 0.6f) + (keywordAlignment * 0.4f);
    }

    /**
     * Calculate traditional job matching score
     */
    private float calculateTraditionalJobScore(UserDetail userDetail, Job job) {
        float score = 0.0f;

        // Location match (40%)
        if (userDetail.getLocation() != null &&
                userDetail.getLocation().equalsIgnoreCase(job.getLocation())) {
            score += 0.4f;
        }

        // Basic description match (30%) - simple keyword matching
        if (userDetail.getDescription() != null && job.getDescription() != null) {
            double similarity = calculateSimpleTextSimilarity(
                    userDetail.getDescription(), job.getDescription());
            score += 0.3f * similarity;
        }

        // Experience level match (30%)
        if (userDetail.getExperience() != null && job.getJobLevel() != null) {
            float expScore = calculateExperienceScore(
                    userDetail.getExperience().getId().intValue(),
                    job.getJobLevel().getName());
            score += 0.3f * expScore;
        }

        return score;
    }

    /**
     * Simple text similarity without NLP
     */
    private double calculateSimpleTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) return 0.0;

        Set<String> words1 = Arrays.stream(text1.toLowerCase().split("\\s+"))
                .filter(word -> word.length() > 2)
                .collect(Collectors.toSet());

        Set<String> words2 = Arrays.stream(text2.toLowerCase().split("\\s+"))
                .filter(word -> word.length() > 2)
                .collect(Collectors.toSet());

        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * Calculate search history green score
     */
    private float calculateSearchHistoryGreenScore(List<String> searchHistory, GreenJobAnalysis jobAnalysis) {
        if (searchHistory.isEmpty()) return 0.0f;

        String combinedSearchHistory = String.join(" ", searchHistory);
        GreenJobAnalysis searchGreenAnalysis = greenJobDetectionService.analyzeGreenJob(
                null, combinedSearchHistory, null);

        return (float) searchGreenAnalysis.getGreenScore();
    }

    // Helper methods
    private String buildUserProfile(UserDetail userDetail, User user) {
        StringBuilder profile = new StringBuilder();

        if (userDetail.getDescription() != null) {
            profile.append(userDetail.getDescription()).append(" ");
        }

        // Add more user profile data if available
        if (userDetail.getLocation() != null) {
            profile.append(userDetail.getLocation()).append(" ");
        }

        return profile.toString().trim();
    }

    private List<String> getRecentSearchHistory(User user) {
        try {
            // Use correct repository method with pagination
            Pageable pageable = PageRequest.of(0, 10);
            return searchHistoryRepository.findByUserOrderByCreatedAtDesc(user)
                    .stream()
                    .map(SearchHistory::getSearchQuery)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting search history for user: {}", user.getEmail(), e);
            return new ArrayList<>();
        }
    }

    private void clearExistingRecommendations(Long jobSeekerId) {
        try {
            // Use correct repository method
            List<JobRecommendation> existing = jobRecommendationRepository.findByJobSeekerIdOrderByScoreDesc(jobSeekerId);
            if (!existing.isEmpty()) {
                jobRecommendationRepository.deleteAll(existing);
            }
        } catch (Exception e) {
            log.error("Error clearing existing recommendations for user: {}", jobSeekerId, e);
        }
    }

    private String getCompanyName(Job job) {
        try {
            if (job.getEmployer() != null && job.getEmployer().getUserDetail() != null) {
                return job.getEmployer().getUserDetail().getCompanyName();
            }
        } catch (Exception e) {
            log.debug("Could not get company name for job: {}", job.getId());
        }
        return null;
    }

    private float calculateExperienceScore(Integer yearsExperience, String jobLevel) {
        if (yearsExperience == null || jobLevel == null) return 0.0f;

        int years = yearsExperience;
        switch (jobLevel.toLowerCase()) {
            case "internship":
            case "thực tập":
                return years <= 1 ? 1.0f : 0.8f;
            case "entrylevel":
            case "entry level":
            case "mới ra trường":
                return years >= 1 && years < 3 ? 1.0f : 0.7f;
            case "midlevel":
            case "mid level":
            case "trung cấp":
                return years >= 3 && years <= 7 ? 1.0f : 0.6f;
            case "highlevel":
            case "high level":
            case "senior":
            case "cao cấp":
                return years >= 7 ? 1.0f : 0.5f;
            default:
                return 0.5f;
        }
    }

    /**
     * Get green job recommendations cho current user
     */
    public List<JobRecommendationResponse> getGreenJobRecommendations() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User jobSeeker = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            List<JobRecommendation> recommendations = jobRecommendationRepository
                    .findByJobSeekerIdOrderByScoreDesc(jobSeeker.getId());

            return recommendations.stream()
                    .map(this::convertToGreenJobResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting green job recommendations", e);
            return new ArrayList<>();
        }
    }

    /**
     * Convert to enhanced response với green job info
     */
    private JobRecommendationResponse convertToGreenJobResponse(JobRecommendation recommendation) {
        try {
            Job job = recommendation.getJob();

            // Analyze green aspects của job
            String companyName = getCompanyName(job);
            GreenJobAnalysis greenAnalysis = greenJobDetectionService.analyzeGreenJob(
                    job.getTitle(), job.getDescription(), companyName);

            JobRecommendationResponse response = new JobRecommendationResponse();
            response.setJobId(job.getId());
            response.setTitle(job.getTitle());
            response.setLocation(job.getLocation());

            if (job.getCategory() != null) {
                response.setCategory(job.getCategory().getName());
            }
            if (job.getJobLevel() != null) {
                response.setJobLevel(job.getJobLevel().getName());
            }
            if (job.getJobType() != null) {
                response.setJobType(job.getJobType().getName());
            }

            response.setScore(recommendation.getScore());

            // Add green job specific fields
            response.setIsGreenJob(greenAnalysis.isGreenJob());
            response.setGreenScore(greenAnalysis.getGreenScore());
            response.setGreenCategories(greenAnalysis.getGreenCategories());
            response.setGreenKeywords(greenAnalysis.getDetectedKeywords());

            return response;
        } catch (Exception e) {
            log.error("Error converting recommendation to response", e);
            // Return basic response without green analysis
            JobRecommendationResponse response = new JobRecommendationResponse();
            response.setJobId(recommendation.getJob().getId());
            response.setTitle(recommendation.getJob().getTitle());
            response.setScore(recommendation.getScore());
            return response;
        }
    }
}