package com.example.jobfinder.service;

import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.JobDocument;
import com.example.jobfinder.repository.ApplicationRepository;
import com.example.jobfinder.repository.JobRepository;
import com.example.jobfinder.repository.JobViewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElasticsearchSyncService {
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchSyncService.class);

    private final JobRepository jobRepository;
    private final JobViewRepository jobViewRepository;
    private final ApplicationRepository applicationRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public ElasticsearchSyncService(JobRepository jobRepository, JobViewRepository jobViewRepository,
                                    ApplicationRepository applicationRepository, ElasticsearchOperations elasticsearchOperations) {
        this.jobRepository = jobRepository;
        this.jobViewRepository = jobViewRepository;
        this.applicationRepository = applicationRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void syncJobsToElasticsearch() {
        log.info("Starting job data sync to Elasticsearch");

        List<Job> jobs = jobRepository.findAll();
        for (Job job : jobs) {
            JobDocument jobDocument = new JobDocument();
            jobDocument.setId(job.getId());
            jobDocument.setTitle(job.getTitle());
            jobDocument.setDescription(job.getDescription());
            jobDocument.setLocation(job.getLocation());
            jobDocument.setEmployerId(job.getEmployer().getId());
            jobDocument.setCategory(job.getCategory().getName());
            jobDocument.setJobLevel(job.getJobLevel().getName());
            jobDocument.setJobType(job.getJobType().getName());

            jobDocument.setViewCount(0);
            jobDocument.setApplicantCount(0);

            elasticsearchOperations.save(jobDocument, IndexCoordinates.of("jobs"));
            log.debug("Indexed job: {}", job.getId());
        }

        log.info("Completed job data sync to Elasticsearch, indexed {} jobs", jobs.size());
    }
}
