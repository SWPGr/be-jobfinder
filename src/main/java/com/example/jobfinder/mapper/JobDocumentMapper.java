package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.model.JobDocument;
import com.example.jobfinder.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobDocumentMapper {

    private final EducationRepository educationRepository;
    private final CategoryRepository categoryRepository;
    private final JobTypeRepository jobTypeRepository;
    private final JobLevelRepository jobLevelRepository;
    private final JobMapper jobMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public JobResponse toJobResponse(JobDocument doc) {
        JobResponse response = jobMapper.toJobResponse(doc);

        // Đảm bảo isSave được map chính xác
        Boolean isSaveValue = doc.getIsSave();
        if (isSaveValue == null) {
            isSaveValue = false;
        }

        log.debug("Mapping isSave from JobDocument: {} for job ID: {}", doc.getIsSave(), doc.getId());
        response.setIsSave(isSaveValue);
        userRepository.findById(doc.getEmployerId()).ifPresent(employer -> {
            response.setEmployer(userMapper.toUserResponse(employer));
        });

        // Map các thông tin khác
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

