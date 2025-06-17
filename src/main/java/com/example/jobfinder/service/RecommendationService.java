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

    /****
     * Constructs a RecommendationService with the specified repositories for users, jobs, user details, job recommendations, categories, job levels, and job types.
     */
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

    /**
     * Generates and saves personalized job recommendations for the authenticated job seeker.
     *
     * Retrieves the current user's details, verifies the "JOB_SEEKER" role, and computes recommendation scores for all available jobs.
     * Deletes any existing recommendations for the user, then saves up to 10 new recommendations with the highest scores above a threshold.
     * Throws a 403 error if the user is not a job seeker, or a 404 error if user details are missing.
     */
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

    /**
     * Retrieves the authenticated job seeker's job recommendations, ordered by relevance.
     *
     * @return a list of job recommendation responses for the current user
     * @throws ResponseStatusException if the user is not a job seeker
     * @throws AppException if the user is not found
     */
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

    /**
     * Calculates a composite relevance score for a job based on the user's profile.
     *
     * The score is determined by weighted factors: experience match (40%), location match (30%), category relevance (20%), and description similarity (10%). The final score is capped at 1.0.
     *
     * @param userDetail the user's detailed profile information
     * @param job the job to evaluate for recommendation
     * @return a float value between 0.0 and 1.0 representing the job's relevance to the user
     */
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

    /**
     * Calculates a similarity score between the user's description and the job description based on overlapping words.
     *
     * The score is the ratio of shared words to the total number of words in the job description, capped at 1.0.
     * Returns 0 if either description is null or if there are no overlapping words.
     *
     * @param userDescription the user's profile description
     * @param jobDescription the job's description
     * @return a float score between 0.0 and 1.0 representing description similarity
     */
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

    /**
     * Calculates a match score between a user's years of experience and a job's required level.
     *
     * @param yearsExperience the user's years of experience
     * @param jobLevel the job's required experience level (e.g., "Internship", "EntryLevel", "MidLevel", "HighLevel")
     * @return a score between 0.0 and 1.0 indicating how well the user's experience matches the job level
     */
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


    /**
     * Converts a JobRecommendation entity into a JobRecommendationResponse DTO.
     *
     * @param recommendation the JobRecommendation to convert
     * @return a JobRecommendationResponse containing job details and recommendation score
     */
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
