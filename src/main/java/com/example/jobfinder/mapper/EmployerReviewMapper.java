package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.employer_review.EmployerReviewRequest;
import com.example.jobfinder.dto.employer_review.EmployerReviewResponse;
import com.example.jobfinder.model.EmployerReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class}) // <-- Quan trọng: uses UserMapper để ánh xạ User
public interface EmployerReviewMapper {

    EmployerReviewMapper INSTANCE = Mappers.getMapper(EmployerReviewMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobSeeker", ignore = true) // Sẽ được set thủ công trong service
    @Mapping(target = "employer", ignore = true)   // Sẽ được set thủ công trong service
    @Mapping(target = "createdAt", ignore = true)  // Sẽ được set tự động bởi @CreationTimestamp
    EmployerReview toEmployerReview(EmployerReviewRequest request);

    @Mapping(source = "jobSeeker", target = "jobSeeker") // Ánh xạ User Entity sang UserResponse DTO
    @Mapping(source = "employer", target = "employer")   // Ánh xạ User Entity sang UserResponse DTO
    EmployerReviewResponse toEmployerReviewResponse(EmployerReview employerReview);

    List<EmployerReviewResponse> toEmployerReviewResponseList(List<EmployerReview> employerReviews);
}