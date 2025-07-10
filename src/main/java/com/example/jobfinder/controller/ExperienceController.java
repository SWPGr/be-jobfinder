// src/main/java/com/example/jobfinder/controller/ExperienceController.java
package com.example.jobfinder.controller;

import com.example.jobfinder.service.BaseNameService;
import com.example.jobfinder.service.ExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/experiences") // Endpoint cho Experience
@RequiredArgsConstructor // Inject ExperienceService
public class ExperienceController extends BaseNameController {

    private final ExperienceService experienceService;

    @Override
    protected BaseNameService getService() {
        return experienceService;
    }

    @Override
    protected String getBasePath() {
        return "Experience"; // Dùng cho log và message
    }
}