package com.example.jobfinder.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.example.jobfinder.dto.JobRecommendationResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.ApplicationRepository;
import com.example.jobfinder.repository.JobRepository;
import com.example.jobfinder.repository.JobViewRepository;
import com.example.jobfinder.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.elasticsearch.index.query.QueryBuilders.*;


import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JobSearchService {
    private static final Logger log = LoggerFactory.getLogger(JobSearchService.class);

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobViewRepository jobViewRepository;
    private final ApplicationRepository applicationRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public JobSearchService(UserRepository userRepository,
                            JobRepository jobRepository,
                            JobViewRepository jobViewRepository,
                            ApplicationRepository applicationRepository,
                            ElasticsearchOperations elasticsearchOperations) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.jobViewRepository = jobViewRepository;
        this.applicationRepository = applicationRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public List<JobRecommendationResponse> searchJobs(String query, int page, int size, String email
            , String location, String category, String jobLevel, String jobType) {
        log.debug("Searching jobs for query: {}, user: {}, page: {}, size: {}", query, email, page, size);

        User jobSeeker = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!jobSeeker.getRole().getName().equals("JOB_SEEKER")) {
            throw new AppException(ErrorCode.INVALID_ROLE);
        }

        List<Job> viewedJobs = jobViewRepository.findByJobSeekerId(jobSeeker.getId())
                .stream().map(jobview -> jobview.getJob()).collect(Collectors.toList());
        List<Long> viewedJobIds = viewedJobs.stream().map(Job::getId).collect(Collectors.toList());
        Set<Long> viewedEmployerIds = viewedJobs.stream()
                .map(job -> job.getEmployer().getId()).collect(Collectors.toSet());
        List<Job> appliedJobs = applicationRepository.findByJobSeekerId(jobSeeker.getId())
                .stream().map(application -> application.getJob()).collect(Collectors.toList());

        BoolQueryBuilder
    }
}
