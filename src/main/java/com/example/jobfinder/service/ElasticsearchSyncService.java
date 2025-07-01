package com.example.jobfinder.service;

import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.JobDocument;
import com.example.jobfinder.repository.ApplicationRepository;
import com.example.jobfinder.repository.JobDocumentRepository;
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
    private final JobDocumentRepository jobDocumentRepository;

    public ElasticsearchSyncService(JobRepository jobRepository, JobDocumentRepository jobDocumentRepository) {
        this.jobRepository = jobRepository;
        this.jobDocumentRepository = jobDocumentRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void syncAllJobs() {
        List<Job> jobs = jobRepository.findAll();
        List<JobDocument> jobDocuments = jobs.stream()
                .map(this::mapToDocument)
                .toList();

        jobDocumentRepository.saveAll(jobDocuments);
    log.info("Completed job data sync to Elasticsearch, indexed {} jobs", jobs.size());
    }

    private JobDocument mapToDocument(Job job) {
        JobDocument doc = new JobDocument();
        doc.setId(job.getId());
        doc.setTitle(job.getTitle());
        doc.setDescription(job.getDescription());
        doc.setLocation(job.getLocation());
        doc.setEmployerId(job.getEmployer().getId());
        doc.setCategory(job.getCategory().getName());
        doc.setJobLevel(job.getJobLevel().getName());
        doc.setJobType(job.getJobType().getName());


        return doc;
    }
}
