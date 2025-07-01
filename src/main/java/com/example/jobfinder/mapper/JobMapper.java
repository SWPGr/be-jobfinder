// D:\Code-Window\JobFinderProject\be-jobfinder\trunglecode\src\main\java\com\example\jobfinder\mapper\JobMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.job.JobCreationRequest;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.job.JobUpdateRequest;
import com.example.jobfinder.model.Job;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {SimpleNameMapper.class, UserMapper.class})

public interface JobMapper {

    @Mapping(target = "id", ignore = true)
    Job toJob(JobCreationRequest request);

    @Mapping(target = "id", ignore = true)
    void updateJob(@MappingTarget Job job, JobUpdateRequest request);


    @Mapping(target = "employer", source = "employer") // Ánh xạ từ Job.employer sang JobResponse.employer
    @Mapping(target = "category", source = "category")
    @Mapping(target = "jobLevel", source = "jobLevel")
    @Mapping(target = "jobType", source = "jobType")
    @Mapping(target = "jobApplicationCounts", ignore = true)
    JobResponse toJobResponse(Job job);

    List<JobResponse> toJobResponseList(List<Job> jobs);
}