package com.example.jobfinder.controller;

import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.job.JobSearchRequest;
import com.example.jobfinder.mapper.JobDocumentMapper;
import com.example.jobfinder.mapper.JobMapper;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.JobDocument;
import com.example.jobfinder.repository.JobDocumentRepository;
import com.example.jobfinder.repository.JobRepository;
import com.example.jobfinder.service.JobSearchService;
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
    private final JobSuggestionService jobSuggestionService;
    private final JobSearchService jobSearchService;
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final JobDocumentMapper jobDocumentMapper;

    public JobSearchController(JobSuggestionService jobSuggestionService, JobSearchService jobSearchService,
                               JobRepository jobRepository, JobMapper jobMapper, JobDocumentMapper jobDocumentMapper) {
        this.jobSuggestionService = jobSuggestionService;
        this.jobSearchService = jobSearchService;
        this.jobRepository = jobRepository;
        this.jobMapper = jobMapper;
        this.jobDocumentMapper = jobDocumentMapper;
    }

    @GetMapping("/search")
    public List<JobResponse> searchJobs(@RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String location,
                                        @RequestParam(required = false) Long categoryId,
                                        @RequestParam(required = false) Long jobLevelId,
                                        @RequestParam(required = false) Long jobTypeId,
                                        @RequestParam(required = false) Long educationId) throws IOException {
        boolean isEmptySearch = (keyword == null || keyword.isEmpty()) && location == null && categoryId == null
                && jobLevelId == null && jobTypeId == null && educationId == null;

        if (isEmptySearch) {
            List<Job> allJobs = jobRepository.findAll();
            return allJobs.stream()
                    .map(jobMapper::toJobResponse)
                    .collect(Collectors.toList());
        }

        JobSearchRequest request = new JobSearchRequest();
        request.setKeyword(keyword);
        request.setLocation(location);
        request.setCategoryId(categoryId);
        request.setJobLevelId(jobLevelId);
        request.setJobTypeId(jobTypeId);
        request.setEducationId(educationId);

        List<JobDocument> documents = jobSearchService.search(request);
        return documents.stream()
                .map(jobDocumentMapper::toJobResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/suggest")
    public List<String> suggest(@RequestParam String keyword) throws IOException {
        return jobSuggestionService.suggestTitles(keyword);
    }
}
