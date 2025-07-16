package com.example.jobfinder.controller;

import com.example.jobfinder.dto.job.JobSearchRequest;
import com.example.jobfinder.dto.job.JobSearchResponse;
import com.example.jobfinder.service.JobSearchService;
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
    private final JobSuggestionService jobSuggestionService;
    private final JobSearchService jobSearchService;

    public JobSearchController(JobSuggestionService jobSuggestionService, JobSearchService jobSearchService) {
        this.jobSuggestionService = jobSuggestionService;
        this.jobSearchService = jobSearchService;
    }

    @GetMapping("/search")
    public JobSearchResponse searchJobs(@RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String location,
                                        @RequestParam(required = false) Long categoryId,
                                        @RequestParam(required = false) Long jobLevelId,
                                        @RequestParam(required = false) Long jobTypeId,
                                        @RequestParam(required = false) Long experienceId,
                                        @RequestParam(required = false) Long educationId,
                                        @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) Float salaryMin,
                                        @RequestParam(required = false) Float salaryMax,
                                        @RequestParam(required = false) Boolean isNegotiable,
                                        @RequestParam(required = false) String sort) throws IOException {

        JobSearchRequest request = new JobSearchRequest();
        request.setKeyword(keyword);
        request.setLocation(location);
        request.setCategoryId(categoryId);
        request.setJobLevelId(jobLevelId);
        request.setJobTypeId(jobTypeId);
        request.setExperienceId(experienceId);
        request.setEducationId(educationId);
        request.setSalaryMin(salaryMin);
        request.setSalaryMax(salaryMax);
        request.setSalaryNegotiable(isNegotiable);
        if (sort != null && !sort.equalsIgnoreCase("asc") && !sort.equalsIgnoreCase("desc")) {
            sort = null;
        }
        request.setSort(sort);
        request.setPage(page);
        request.setSize(size);

        if ((keyword == null || keyword.isEmpty()) && location == null && categoryId == null
                && jobLevelId == null && jobTypeId == null && educationId == null 
                && salaryMin == null && salaryMax == null && isNegotiable == null) {

            return jobSearchService.searchWithIsSaveStatus(request);
        }

        return jobSearchService.searchWithIsSaveStatus(request);
    }

    @GetMapping("/suggest")
    public List<String> suggest(@RequestParam String keyword) throws IOException {
        return jobSuggestionService.suggestTitles(keyword);
    }
}
