package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.application.ApplicationRequest;
import com.example.jobfinder.dto.application.ApplicationResponse;
import com.example.jobfinder.model.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {UserMapper.class, JobMapper.class}) // <-- Cần UserMapper và JobMapper
public interface ApplicationMapper {
    ApplicationMapper INSTANCE = Mappers.getMapper(ApplicationMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobSeeker", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "status", ignore = true) // Sẽ được set thủ công
    @Mapping(target = "appliedAt", ignore = true) // Sẽ được set thủ công
    Application toApplication(ApplicationRequest request);

    // Ánh xạ từ Entity sang Response DTO
    @Mapping(source = "jobSeeker", target = "jobSeeker")
    @Mapping(source = "job", target = "job")
    @Mapping(source = "status", target = "status")
    ApplicationResponse toApplicationResponse(Application application);

    // Thêm phương thức ánh xạ list nếu cần
    // List<ApplicationResponse> toApplicationResponseList(List<Application> applications);
}