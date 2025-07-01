package com.example.jobfinder.controller;

import com.example.jobfinder.model.JobDocument;
import com.example.jobfinder.repository.JobDocumentRepository;
import com.example.jobfinder.service.JobSuggestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/jobs/")
public class JobSearchController {
    private final JobDocumentRepository jobDocumentRepository;
    private final JobSuggestionService jobSuggestionService;

    public JobSearchController(JobDocumentRepository jobDocumentRepository, JobSuggestionService jobSuggestionService) {
        this.jobDocumentRepository = jobDocumentRepository;
        this.jobSuggestionService = jobSuggestionService;
    }

    @GetMapping("/search")
    public List<JobDocument> searchJobs(@RequestParam String keyword) {
        return jobDocumentRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @GetMapping("/suggest")
    public List<String> suggest(@RequestParam String keyword) throws IOException {
        return jobSuggestionService.suggestTitles(keyword);
    }
}
