// src/main/java/com/example/jobfinder/mapper/JobSeekerMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.user.JobSeekerResponse;
import com.example.jobfinder.model.UserDetail; // Bây giờ map từ UserDetail
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JobSeekerMapper {
    // Ánh xạ từ UserDetail entity
    @Mapping(source = "user.email", target = "userEmail") // Lấy email từ User qua UserDetail
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "location", target = "location")

    // Các trường riêng cho JobSeeker từ UserDetail
    @Mapping(source = "experience.id", target = "experienceId")
    @Mapping(source = "experience.name", target = "experienceName")
    @Mapping(source = "resumeUrl", target = "resumeUrl")

    // Ánh xạ thông tin Education (nếu có)
    @Mapping(source = "education.id", target = "educationId")
    @Mapping(source = "education.name", target = "educationName") // Giả sử Education có trường 'name'
    JobSeekerResponse toJobSeekerResponse(UserDetail userDetail); // Map từ UserDetail
}