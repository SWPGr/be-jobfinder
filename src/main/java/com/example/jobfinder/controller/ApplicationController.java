package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApplicationRequest;
import com.example.jobfinder.model.Application;
import com.example.jobfinder.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/apply")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Application> applyJob(@RequestBody ApplicationRequest request) throws Exception {
        Application application = applicationService.applyJob(request);
        return ResponseEntity.ok(application);
    }
}
