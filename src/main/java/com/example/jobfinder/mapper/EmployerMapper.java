// src/main/java/com/example/jobfinder/mapper/EmployerMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.user.EmployerResponse;
import com.example.jobfinder.model.UserDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployerMapper {
    // Ánh xạ từ UserDetail entity
    @Mapping(source = "user.id", target = "userId") // <-- Lấy ID từ User
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "location", target = "location")

    // Các trường riêng cho Employer từ UserDetail
    @Mapping(source = "companyName", target = "companyName")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "website", target = "website")

    EmployerResponse toEmployerResponse(UserDetail userDetail);
}