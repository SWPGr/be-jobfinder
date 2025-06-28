package com.example.jobfinder.controller;

import com.example.jobfinder.dto.job.JobOptionsResponse;
import com.example.jobfinder.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/options")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OptionController {
    JobTypeRepository jobTypeRepository;
    JobLevelRepository jobLevelRepository;
    EducationRepository educationRepository;
    CategoryRepository categoryRepository;
    ExperienceRepository experienceRepository;

    @GetMapping("/all")
    public JobOptionsResponse getAllOptions() {
        return new JobOptionsResponse(
                jobTypeRepository.findAll(),
                jobLevelRepository.findAll(),
                educationRepository.findAll(),
                categoryRepository.findAll(),
                experienceRepository.findAll()
        );
    }
}
