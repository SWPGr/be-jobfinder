package com.example.jobfinder.controller;

import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.mapper.JobMapper;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.JobDocument;
import com.example.jobfinder.repository.JobDocumentRepository;
import com.example.jobfinder.repository.JobRepository;
import com.example.jobfinder.service.JobService;
import com.example.jobfinder.service.JobSuggestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("api/jobs/")
public class JobSearchController {
    private final JobDocumentRepository jobDocumentRepository;
    private final JobSuggestionService jobSuggestionService;
    private final JobService jobService;
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    public JobSearchController(JobDocumentRepository jobDocumentRepository, JobSuggestionService jobSuggestionService,
                               JobService jobService, JobRepository jobRepository, JobMapper jobMapper) {
        this.jobDocumentRepository = jobDocumentRepository;
        this.jobSuggestionService = jobSuggestionService;
        this.jobService = jobService;
        this.jobRepository = jobRepository;
        this.jobMapper = jobMapper;
    }

    @GetMapping("/search")
    public List<JobResponse> searchJobs(@RequestParam(required = false) String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // Không có keyword → lấy từ DB, ánh xạ sang JobResponse
            List<Job> allJobs = jobRepository.findAll();
            return allJobs.stream()
                    .map(jobMapper::toJobResponse)
                    .collect(Collectors.toList());
        }

        // Có keyword → tìm từ Elasticsearch (JobDocument), ánh xạ sang JobResponse nếu cần
        List<JobDocument> documents = jobDocumentRepository.findByTitleContainingIgnoreCase(keyword);
        return documents.stream()
                .map(jobMapper::toJobResponse) // ánh xạ JobDocument → JobResponse
                .collect(Collectors.toList());
    }

    @GetMapping("/suggest")
    public List<String> suggest(@RequestParam String keyword) throws IOException {
        return jobSuggestionService.suggestTitles(keyword);
    }
}
