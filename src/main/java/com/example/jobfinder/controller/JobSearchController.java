package com.example.jobfinder.controller;

import com.example.jobfinder.model.JobDocument;
import com.example.jobfinder.repository.JobDocumentRepository;
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

    public JobSearchController(JobDocumentRepository jobDocumentRepository,
                               JobSuggestionService jobSuggestionService,
                               JobService jobService) {
        this.jobDocumentRepository = jobDocumentRepository;
        this.jobSuggestionService = jobSuggestionService;
        this.jobService = jobService;
    }

    @GetMapping("/search")
    public List<JobDocument> searchJobs(@RequestParam(required = false) String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            Iterable<JobDocument> allJobs = jobDocumentRepository.findAll();
            return StreamSupport.stream(allJobs.spliterator(), false)
                    .collect(Collectors.toList());
        }
        return jobDocumentRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @GetMapping("/suggest")
    public List<String> suggest(@RequestParam String keyword) throws IOException {
        return jobSuggestionService.suggestTitles(keyword);
    }
}
