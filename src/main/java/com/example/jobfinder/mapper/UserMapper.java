// src/main/java/com/example/jobfinder/mapper/UserMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.model.Education;
import com.example.jobfinder.model.Experience;
import com.example.jobfinder.model.User;
import com.example.jobfinder.model.UserDetail; // Cần import UserDetail
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.dto.user.UserCreationRequest; // Cần import UserCreationRequest
import com.example.jobfinder.dto.user.UserUpdateRequest; // Cần import UserUpdateRequest
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE) // Bỏ qua các trường null khi map
public interface UserMapper {

    // --- Mapper từ Request DTO sang Entity để tạo mới User ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "userDetail", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "resetPasswordToken", ignore = true) // Add this
    @Mapping(target = "resetPasswordExpiry", ignore = true) // Add this
    @Mapping(target = "verificationToken", ignore = true) // Add this
    @Mapping(target = "verified", defaultValue = "0") // Keep default value for 'verified'
    @Mapping(target = "isPremium", defaultValue = "false") // Keep default value for 'isPremium'
    @Mapping(target = "subscriptions", ignore = true) // Add this for collections
    @Mapping(target = "applications", ignore = true) // Add this for collections
    @Mapping(target = "postedJobs", ignore = true) // Add this for collections
    @Mapping(target = "jobRecommendations", ignore = true) // Add this for collections
    @Mapping(target = "savedJobs", ignore = true) // Add this for collections
    @Mapping(target = "jobViews", ignore = true) // Add this for collections
    @Mapping(target = "reviewsGiven", ignore = true) // Add this for collections
    @Mapping(target = "reviewsReceived", ignore = true) // Add this for collections
    @Mapping(target = "notifications", ignore = true) // Add this for collections
    @Mapping(target = "chatbotHistories", ignore = true)
    User toUser(UserCreationRequest request);

    // --- Mapper từ Entity sang Response DTO để hiển thị thông tin User ---
    @Mapping(source = "role.name", target = "roleName")
    @Mapping(source = "userDetail.fullName", target = "fullName")
    @Mapping(source = "userDetail.phone", target = "phone")
    @Mapping(source = "userDetail.location", target = "location")
    @Mapping(source = "verified", target = "verified")
    @Mapping(source = "isPremium", target = "isPremium")
    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss") // Ánh xạ LocalDateTime sang String
    @Mapping(source = "updatedAt", target = "updatedAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss") // Ánh xạ LocalDateTime sang String
    @Mapping(source = "userDetail.avatarUrl", target = "avatarUrl")
    @Mapping(source = "userDetail.companyName", target = "companyName")
    @Mapping(source = "userDetail.website", target = "website")
    @Mapping(source = "userDetail.banner", target = "banner")
    @Mapping(source = "userDetail.teamSize", target = "teamSize")
    @Mapping(source = "userDetail.yearOfEstablishment", target = "yearOfEstablishment")
    @Mapping(source = "userDetail.mapLocation", target = "mapLocation")
    @Mapping(source = "userDetail.organizationType", target = "organizationType")
    @Mapping(source = "userDetail.resumeUrl", target = "resumeUrl") // <-- Sửa lại tên trường trong UserDetail
    @Mapping(source = "userDetail.education", target = "education", qualifiedByName = "mapEducationToSimpleNameResponse")
    @Mapping(source = "userDetail.experience", target = "experience", qualifiedByName = "mapExperienceToSimpleNameResponse")
    UserResponse toUserResponse(User user);

    // --- Mapper để cập nhật User Entity từ Update Request DTO ---
    // @MappingTarget chỉ định đối tượng User hiện có sẽ được cập nhật
    // nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE sẽ giúp bỏ qua các trường null trong request
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "userDetail", ignore = true) // UserDetail được xử lý riêng trong Service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "resetPasswordToken", ignore = true)
    @Mapping(target = "resetPasswordExpiry", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "verified", source = "verified", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE) // Cập nhật verified nếu có trong request
    @Mapping(target = "isPremium", source = "isPremium", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE) // Cập nhật isPremium nếu có trong request
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "postedJobs", ignore = true)
    @Mapping(target = "jobRecommendations", ignore = true)
    @Mapping(target = "savedJobs", ignore = true)
    @Mapping(target = "jobViews", ignore = true)
    @Mapping(target = "reviewsGiven", ignore = true)
    @Mapping(target = "reviewsReceived", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "chatbotHistories", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    List<UserResponse> toUserResponseList(List<User> users);

    default Boolean map(Integer value) {
        if (value == null) {
            return null; // Trả về null nếu giá trị gốc là null
        }
        return value != 0; // Nếu 0 là false, các giá trị khác (thường là 1) là true.
    }

    default Integer map(Boolean value) {
        if (value == null) {
            return null; // Trả về null nếu giá trị gốc là null
        }
        return value ? 1 : 0; // True thành 1, False thành 0.
    }

    // Helper method để ánh xạ Education sang SimpleNameResponse
    @Named("mapEducationToSimpleNameResponse")
    default SimpleNameResponse mapEducationToSimpleNameResponse(Education education) {
        if (education == null) {
            return null;
        }
        return SimpleNameResponse.builder()
                .id(education.getId())
                .name(education.getName())
                .build();
    }

    // <-- THÊM HELPER METHOD NÀY ĐỂ ÁNH XẠ EXPERIENCE SANG SimpleNameResponse -->
    @Named("mapExperienceToSimpleNameResponse")
    default SimpleNameResponse mapExperienceToSimpleNameResponse(Experience experience) {
        if (experience == null) {
            return null;
        }
        return SimpleNameResponse.builder()
                .id(experience.getId())
                .name(experience.getName())
                .build();
    }
}