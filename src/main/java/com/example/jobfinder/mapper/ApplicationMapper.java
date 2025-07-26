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
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "resume", ignore = true)
    @Mapping(target = "coverLetter", ignore = true)
    Application toApplication(ApplicationRequest request);

    // Ánh xạ từ Entity sang Response DTO
    @Mapping(source = "jobSeeker.userDetail", target = "jobSeeker")
    @Mapping(source = "job", target = "job")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "resume", target = "resume")
    @Mapping(source = "appliedAt", target = "appliedAt")
    @Mapping(source = "coverLetter", target = "coverLetter")
    ApplicationResponse toApplicationResponse(Application application);

    @Mapping(source = "job.id", target = "job.id") // Đảm bảo map đúng JobResponse
    @Mapping(target = "jobSeeker", ignore = true) // Bỏ qua việc map trường jobSeeker
    @Mapping(source = "resume", target = "resume") // Nếu tên trường trong Entity là resumeUrl
    ApplicationResponse toApplicationResponseWithoutJobSeeker(Application application);

    // Thêm phương thức ánh xạ list nếu cần
    // List<ApplicationResponse> toApplicationResponseList(List<Application> applications);
}