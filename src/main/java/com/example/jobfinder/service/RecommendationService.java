package com.example.jobfinder.service;

import com.example.jobfinder.dto.job.JobRecommendationResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.*;
import com.example.jobfinder.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final ApplicationRepository applicationRepository;
    private final JobLevelRepository jobLevelRepository;
    private final JobTypeRepository jobTypeRepository;
    private final JobViewRepository jobViewRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public RecommendationService(UserRepository userRepository,
                                 JobRepository jobRepository,
                                 UserDetailsRepository userDetailsRepository,
                                 JobRecommendationRepository jobRecommendationRepository,
                                 CategoryRepository categoryRepository,
                                 ApplicationRepository applicationRepository,
                                 JobLevelRepository jobLevelRepository,
                                 JobTypeRepository jobTypeRepository,
                                 JobViewRepository jobViewRepository,
                                 ElasticsearchOperations elasticsearchOperations) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.jobRecommendationRepository = jobRecommendationRepository;
        this.categoryRepository = categoryRepository;
        this.applicationRepository = applicationRepository;
        this.jobLevelRepository = jobLevelRepository;
        this.jobTypeRepository = jobTypeRepository;
        this.jobViewRepository = jobViewRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void generateRecommendations(){
        log.debug("Generating job recommendations for all JOB_SEEKER users");

        List<User> jobSeekers = userRepository.findAll().stream()
                .filter(user -> user.getRole() != null && "JOB_SEEKER".equalsIgnoreCase(user.getRole().getName()))
                .collect(Collectors.toList());
        for (User jobSeeker : jobSeekers) {
            try {
                generateRecommendationsForUser(jobSeeker);
            } catch (AppException e) {
                log.error("Application error for user {}: {}", jobSeeker.getEmail(), e.getMessage());
            } catch (Exception e) {
                log.error("Failed to generate recommendations for user: {}", jobSeeker.getEmail(), e);
            }
        }
    }
    @Transactional
    public void generateRecommendationsForUser(User jobSeeker) {
        log.debug("Generating recommendations for user: {}", jobSeeker.getEmail());

        UserDetail userDetail = userDetailsRepository.findByUserId(jobSeeker.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND)); // UserDetail phải tồn tại.;

            List<Job> jobs = jobRepository.findAll();
            List<Job> viewedJobs = jobViewRepository.findByJobSeekerId(jobSeeker.getId())
                .stream().map(jobView -> jobView.getJob()).collect(Collectors.toList());
            List<Long> viewedJobIds = viewedJobs.stream().map(Job::getId).collect(Collectors.toList());
            Set<Long> viewedEmployerIds = viewedJobs.stream()
                .map(job -> job.getEmployer().getId()).collect(Collectors.toSet());
            List<Job> appliedJobs = applicationRepository.findByJobSeekerId(jobSeeker.getId())
                .stream().map(application -> application.getJob()).collect(Collectors.toList());

            String queryString = String.format(
                    "category:\"%s\"^2 OR location:\"%s\"^2 OR employerId:(%s)^3",
                    userDetail.getDescription() != null ? userDetail.getDescription().toLowerCase() : "",
                    userDetail.getLocation() != null ? userDetail.getLocation().toLowerCase() : "",
                    viewedEmployerIds.stream().map(String::valueOf).collect(Collectors.joining(" "))
            );
            Query query = new StringQuery(queryString);
        SearchHits<JobDocument> searchHits;
        try {
            searchHits = elasticsearchOperations.search(query, JobDocument.class);
        } catch (Exception e) {
            log.error("Elasticsearch query failed: {}", e.getMessage());
            throw new AppException(ErrorCode.ELASTICSEARCH_ERROR);
        }

            jobRecommendationRepository.deleteByJobSeekerId(jobSeeker.getId());

            List<JobRecommendation> recommendations = new ArrayList<>();
        for (Job job : jobRepository.findAll()) {
            float score = calculateJobScore(userDetail, job, viewedJobIds, appliedJobs, viewedEmployerIds, 0.5f);
            log.info("Manual Score for job '{}': {}", job.getTitle(), score);


                log.info("Job: {}, Score: {}", job.getTitle(), score);
                if (score >= 0.3) {
                    JobRecommendation recommendation = new JobRecommendation();
                    recommendation.setJobSeeker(jobSeeker);
                    recommendation.setJob(job);
                    recommendation.setScore(score);
                    recommendation.setRecommendedAt(LocalDateTime.now());
                    recommendations.add(recommendation);
                }
            }

        List<JobRecommendation> topRecommendations = recommendations.stream()
                .sorted(Comparator.comparing(JobRecommendation::getScore).reversed())
                .limit(10)
                .collect(Collectors.toList());
        jobRecommendationRepository.saveAll(topRecommendations);

        log.debug("Saved {} recommendations for user: {}", topRecommendations.size(), jobSeeker.getEmail());

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

    private float calculateJobScore(UserDetail userDetail, Job job, List<Long> viewedJobIds,
                                    List<Job> appliedJobs, Set<Long> viewedEmployerIds, float esScore) {
        float score = 0.0f;

        float experienceScore = calculateExperienceScore(userDetail.getExperience().getId().intValue(), job.getJobLevel().getName());
        score += 0.25f * experienceScore;

        float locationScore = userDetail.getLocation() != null && userDetail.getLocation()
                .equalsIgnoreCase(job.getLocation()) ? 1.0f : 0.0f;
        score += 0.20f * locationScore;

        float categoryScore = userDetail.getDescription() != null && userDetail.getDescription().toLowerCase()
                .contains(job.getCategory().getName().toLowerCase()) ? 1.0f : 0.5f;
        score += 0.15f * categoryScore;

        float descriptionScore = calculateDescriptionScore(userDetail.getDescription(), job.getDescription());
        score += 0.05f * descriptionScore;

        float viewScore = viewedJobIds.contains(job.getId()) ? 1.0f : 0.0f;
        score += 0.10f * viewScore;

        float applicationScore = calculateApplicationScore(job, appliedJobs);
        score += 0.10f * applicationScore;
        
        float employerViewScore = viewedEmployerIds.contains(job.getEmployer().getId()) ? 1.0f : 0.0f;
        score += 0.10f * employerViewScore;

        float normalizedEsScore = Math.min(esScore / 10.0f, 1.0f);
        score += 0.15f * normalizedEsScore;

        return Math.min(score, 1.0f);
    }

    private float calculateApplicationScore(Job job, List<Job> appliedJobs) {
        if (appliedJobs.isEmpty()) {
            return 0.0f;
        }

        boolean hasSimilarCategory = appliedJobs.stream()
                .anyMatch(appliedJob -> appliedJob.getCategory().getName().equalsIgnoreCase(job.getCategory().getName()));
        boolean hasSimilarLocation = appliedJobs.stream()
                .anyMatch(appliedJob -> appliedJob.getLocation().equalsIgnoreCase(job.getLocation()));

        float score = 0.0f;
        if (hasSimilarCategory) score += 0.5f;
        if (hasSimilarLocation) score += 0.5f;
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
