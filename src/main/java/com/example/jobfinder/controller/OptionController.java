package com.example.jobfinder.controller;

import com.example.jobfinder.dto.job.*;
import com.example.jobfinder.repository.*;
import jakarta.persistence.Cacheable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/options")
@RequiredArgsConstructor
@Cacheable()
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OptionController {
    JobTypeRepository jobTypeRepository;
    JobLevelRepository jobLevelRepository;
    EducationRepository educationRepository;
    CategoryRepository categoryRepository;
    ExperienceRepository experienceRepository;
    OrganizationRepository organizationRepository;

    @GetMapping("/all")
    public JobOptionsResponse getAllOptions() {
        return new JobOptionsResponse(
                jobTypeRepository.findAll().stream().map(JobTypeDTO::fromEntity).toList(),
                jobLevelRepository.findAll().stream().map(JobLevelDTO::fromEntity).toList(),
                educationRepository.findAll().stream().map(EducationDTO::fromEntity).toList(),
                categoryRepository.findAll().stream().map(CategoryDTO::fromEntity).toList(),
                experienceRepository.findAll().stream().map(ExperienceDTO::fromEntity).toList(),
                organizationRepository.findAll().stream().map(OrganizationDTO::fromEntity).toList()
        );
    }
}
