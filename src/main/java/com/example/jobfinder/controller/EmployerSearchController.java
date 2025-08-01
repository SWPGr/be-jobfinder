package com.example.jobfinder.controller;

import com.example.jobfinder.dto.employer.EmployerSearchRequest;
import com.example.jobfinder.dto.employer.EmployerSearchResponse;
import com.example.jobfinder.service.EmployerSearchService;
import com.example.jobfinder.service.EmployerSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/employers")
@RequiredArgsConstructor
public class EmployerSearchController {
    
    private final EmployerSearchService employerSearchService;
    private final EmployerSuggestionService employerSuggestionService;

    @GetMapping("/search")
    public EmployerSearchResponse searchEmployers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) throws IOException {

        EmployerSearchRequest request = EmployerSearchRequest.builder()
                .name(name)
                .location(location)
                .organizationId(organizationId)
                .keyword(keyword)
                .page(page)
                .size(size)
                .build();

        return employerSearchService.search(request);
    }

    // Suggestion endpoints
    @GetMapping("/suggest")
    public List<String> suggestCompanyNames(@RequestParam String keyword) {
        return employerSuggestionService.suggestCompanyNames(keyword);
    }
}
