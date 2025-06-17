package com.example.jobfinder.service;

import com.example.jobfinder.dto.JobRecommendationResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.JobRecommendation;
import com.example.jobfinder.model.User;
import com.example.jobfinder.model.UserDetail;
import com.example.jobfinder.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class RecommendationService {
    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final JobRecommendationRepository jobRecommendationRepository;
    private final CategoryRepository categoryRepository;
    private final JobLevelRepository jobLevelRepository;
    private final JobTypeRepository jobTypeRepository;

    public RecommendationService(UserRepository userRepository, JobRepository jobRepository,
                                 UserDetailsRepository userDetailsRepository, JobRecommendationRepository jobRecommendationRepository,
                                 CategoryRepository categoryRepository, JobLevelRepository jobLevelRepository,
                                 JobTypeRepository jobTypeRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.jobRecommendationRepository = jobRecommendationRepository;
        this.categoryRepository = categoryRepository;
        this.jobLevelRepository = jobLevelRepository;
        this.jobTypeRepository = jobTypeRepository;
    }

    @Transactional
    public void generateRecommendations() {
        log.debug("Generating recommendations");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.debug("Authenticated email: {}", email);
        User jobSeeker = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String role = jobSeeker.getRole().getName();
        if (!role.equals("JOB_SEEKER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only JOB_SEEKER can view recommendations");
        }

        UserDetail userDetail = userDetailsRepository.findByUserId(jobSeeker.getId());
        if (userDetail == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User details not found");
        }

        synchronized (this.getClass()) {
            List<Job> jobs = jobRepository.findAll();

            jobRecommendationRepository.deleteByJobSeekerId(jobSeeker.getId());

            List<JobRecommendation> recommendations = new ArrayList<>();
            for (Job job : jobs) {
                float score = calculateJobScore(userDetail, job);
                if (score > 0.3) {
                    JobRecommendation recommendation = new JobRecommendation();
                    recommendation.setJobSeeker(jobSeeker);
                    recommendation.setJob(job);
                    recommendation.setScore(score);
                    recommendation.setRecommendedAt(LocalDateTime.now());
                    recommendations.add(recommendation);
                }
            }

            recommendations.stream()
                    .sorted(Comparator.comparing(JobRecommendation::getScore).reversed())
                    .limit(10)
                    .forEach(recommendation -> {
                        Optional<JobRecommendation> existing = jobRecommendationRepository
                                .findByJobSeekerIdAndJobId(jobSeeker.getId(), recommendation.getJob().getId());
                        if (existing.isPresent()) {
                            log.debug("Updating existing recommendation for user: {}, job: {}",
                                    jobSeeker.getId(), recommendation.getJob().getId());
                            JobRecommendation r = existing.get();
                            r.setScore(recommendation.getScore());
                            r.setRecommendedAt(LocalDateTime.now());
                            jobRecommendationRepository.save(r);
                        } else {
                            log.debug("Saving new recommendation for user: {}, job: {}",
                                    jobSeeker.getId(), recommendation.getJob().getId());
                            jobRecommendationRepository.save(recommendation);
                        }
                    });

            log.debug("Generated {} recommendations for user: {}", recommendations.size(), jobSeeker.getId());
        }
    }

    public List<JobRecommendationResponse> getRecommendations() {
        log.debug("Fetching job recommendations");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User jobSeeker = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String role = jobSeeker.getRole().getName();
        if(!role.equals("JOB_SEEKER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only JOB_SEEKER can view recommendations");
        }


        List<JobRecommendation> recommendations  = jobRecommendationRepository.findByJobSeekerIdOrderByScoreDesc(jobSeeker.getId());
        return recommendations.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    private float calculateJobScore(UserDetail userDetail, Job job) {
        float score = 0.0f;

        float experienceScore = calculateExperienceScore(userDetail.getYearsExperience(), job.getJobLevel().getName());
        score += 0.4f * experienceScore;

        float locationScore = userDetail.getLocation() != null && userDetail.getLocation()
                .equalsIgnoreCase(job.getLocation()) ? 1.0f : 0.0f;
        score += 0.3f * locationScore;

        float categoryScore = userDetail.getDescription() != null && userDetail.getDescription().toLowerCase()
                .contains(job.getCategory().getName().toLowerCase()) ? 1.0f : 0.5f;
        score += 0.2f * categoryScore;

        float descriptionScore = calculateDescriptionScore(userDetail.getDescription(), job.getDescription());
        score += 0.1f * descriptionScore;

        return Math.min(score, 1.0f);
    }

    private float calculateDescriptionScore(String userDescription, String jobDescription) {
        if(userDescription == null || jobDescription == null) {
            return 0.0f;
        }
        Set<String> userWords = Arrays.stream(userDescription.toLowerCase().split("\\s+"))
                .collect(Collectors.toSet());
        Set<String> jobWords = Arrays.stream(jobDescription.toLowerCase().split("\\s+"))
                .collect(Collectors.toSet());
        userWords.retainAll(jobWords);
        return userWords.size() > 0 ? Math.min((float) userWords.size() / jobWords.size(), 1.0f) : 0.0f;
    }

    private float calculateExperienceScore(Integer yearsExperience, String jobLevel) {
        if(yearsExperience == null) {
            return 0.0f;
        }
        int years = yearsExperience;
        switch (jobLevel.toLowerCase()) {
            case "internship" :
                return years <= 1 ? 1.0f : 0.8f;
            case "entrylevel" :
                return years >= 1 && years < 3 ? 1.0f : 0.7f;
            case "midlevel" :
                return years >= 3 && years <= 7 ? 1.0f : 0.6f;
            case "highlevel" :
                return years >= 7 ? 1.0f : 0.5f;
            default:
                return 0.5f;
        }
    }


    private JobRecommendationResponse convertToResponse(JobRecommendation recommendation) {
        JobRecommendationResponse response = new JobRecommendationResponse();

        Job job = recommendation.getJob();
        response.setJobId(job.getId());
        response.setTitle(job.getTitle());
        response.setLocation(job.getLocation());
        response.setCategory(job.getCategory().getName());
        response.setJobLevel(job.getJobLevel().getName());
        response.setJobType(job.getJobType().getName());
        response.setScore(recommendation.getScore());

        return response;
    }
}
