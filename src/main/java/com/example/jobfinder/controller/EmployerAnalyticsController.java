package com.example.jobfinder.controller;

import com.example.jobfinder.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics/employer")
@RequiredArgsConstructor
public class EmployerAnalyticsController {

    private final ApplicationService jobApplicationService;

}