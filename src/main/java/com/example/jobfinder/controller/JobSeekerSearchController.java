package com.example.jobfinder.controller;

import com.example.jobfinder.dto.job_seeker.JobSeekerSearchRequest;
import com.example.jobfinder.dto.job_seeker.JobSeekerSearchResponse;
import com.example.jobfinder.service.JobSeekerSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/job-seekers")
@RequiredArgsConstructor
public class JobSeekerSearchController {
    
    private final JobSeekerSearchService jobSeekerSearchService;

    @GetMapping("/search")
    public JobSeekerSearchResponse searchJobSeekers(
            @RequestParam(required = false) Long educationId,
            @RequestParam(required = false) Long experienceId,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) throws IOException {

        JobSeekerSearchRequest request = JobSeekerSearchRequest.builder()

                .educationId(educationId)
                .experienceId(experienceId)
                .location(location)
                .page(page)
                .size(size)
                .build();

        return jobSeekerSearchService.search(request);
    }
}
