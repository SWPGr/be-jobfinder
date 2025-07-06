package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.model.JobDocument;
import com.example.jobfinder.repository.CategoryRepository;
import com.example.jobfinder.repository.EducationRepository;
import com.example.jobfinder.repository.JobLevelRepository;
import com.example.jobfinder.repository.JobTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobDocumentMapper {

    private final EducationRepository educationRepository;
    private final CategoryRepository categoryRepository;
    private final JobTypeRepository jobTypeRepository;
    private final JobLevelRepository jobLevelRepository;
    private final JobMapper jobMapper;

    public JobResponse toJobResponse(JobDocument doc) {
        JobResponse response = jobMapper.toJobResponse(doc);

        response.setEducation(getEducation(doc.getEducationId()));
        response.setCategory(getCategory(doc.getCategoryId()));
        response.setJobType(getJobType(doc.getJobTypeId()));
        response.setJobLevel(getJobLevel(doc.getJobLevelId()));

        return response;
    }

    private SimpleNameResponse getEducation(Long id) {
        return educationRepository.findById(id)
                .map(e -> new SimpleNameResponse(e.getId(), e.getName()))
                .orElse(null);
    }

    private SimpleNameResponse getCategory(Long id) {
        return categoryRepository.findById(id)
                .map(c -> new SimpleNameResponse(c.getId(), c.getName()))
                .orElse(null);
    }

    private SimpleNameResponse getJobType(Long id) {
        return jobTypeRepository.findById(id)
                .map(j -> new SimpleNameResponse(j.getId(), j.getName()))
                .orElse(null);
    }

    private SimpleNameResponse getJobLevel(Long id) {
        return jobLevelRepository.findById(id)
                .map(l -> new SimpleNameResponse(l.getId(), l.getName()))
                .orElse(null);
    }
}

