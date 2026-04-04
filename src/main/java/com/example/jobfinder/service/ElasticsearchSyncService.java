package com.example.jobfinder.service;

import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.JobDocument;
import com.example.jobfinder.repository.ApplicationRepository;
import com.example.jobfinder.repository.JobDocumentRepository;
import com.example.jobfinder.repository.JobRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ElasticsearchSyncService {
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchSyncService.class);
    private static final DateTimeFormatter CREATED_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter EXPIRED_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private JobRepository jobRepository;
    private JobDocumentRepository jobDocumentRepository;
    private ApplicationRepository applicationRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void syncAllJobs() {
        List<Job> jobs = jobRepository.findAll();
        List<JobDocument> jobDocuments = jobs.stream()
                .map(job -> {
                    Long count = applicationRepository.countByJob_Id(job.getId());
                    return mapToDocument(job, count);
                })
                .toList();
        jobDocumentRepository.saveAll(jobDocuments);
    log.info("Completed job data sync to Elasticsearch, indexed {} jobs", jobs.size());
    }

    private JobDocument mapToDocument(Job job, Long applicationCount) {
        JobDocument doc = new JobDocument();
        doc.setId(job.getId());
        doc.setTitle(job.getTitle());
        doc.setDescription(job.getDescription());
        doc.setLocation(job.getLocation());
        doc.setEmployerId(job.getEmployer().getId());
        doc.setCategoryId(job.getCategory().getId());
        doc.setJobLevelId(job.getJobLevel().getId());
        doc.setSalaryMin(job.getSalaryMin());
        doc.setSalaryMax(job.getSalaryMax());
        doc.setJobTypeId(job.getJobType().getId());
        doc.setEducationId(job.getEducation().getId());
        if (job.getExperience() != null) {
            doc.setExperience(job.getExperience().getId());
        }
        doc.setActive(job.getActive());
        doc.setIsSave(false);
        doc.setExpiredDate(job.getExpiredDate() != null
                ? job.getExpiredDate().format(EXPIRED_DATE_FORMATTER)
                : null);

        doc.setCreatedAt(job.getCreatedAt() != null
                ? job.getCreatedAt().format(CREATED_AT_FORMATTER)
                : null);
        log.info("Job {} createdAt raw: {}", job.getId(), job.getCreatedAt());
        doc.setJobApplicationCounts(applicationCount);
        return doc;
    }
}
