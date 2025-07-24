// src/main/java/com/example/jobfinder/mapper/UserMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.user.UserDto;
import com.example.jobfinder.model.User;
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.dto.user.UserCreationRequest;
import com.example.jobfinder.dto.user.UserUpdateRequest;
import com.example.jobfinder.model.UserDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget; // Cần import MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy;

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
    @Mapping(target = "chatbotHistories", ignore = true) // Add this for collections
    User toUser(UserCreationRequest request);

    // --- Mapper từ Entity sang Response DTO để hiển thị thông tin User ---
    @Mapping(target = "role", ignore = true) // Will be handled separately
    @Mapping(source = "userDetail.fullName", target = "fullName")
    @Mapping(source = "userDetail.phone", target = "phone")
    @Mapping(source = "userDetail.location", target = "location")
    @Mapping(source = "userDetail.companyName", target = "companyName")
    @Mapping(source = "userDetail.website", target = "website")
    @Mapping(source = "userDetail.banner", target = "banner")
    @Mapping(source = "userDetail.teamSize", target = "teamSize")
    @Mapping(source = "userDetail.yearOfEstablishment", target = "yearOfEstablishment")
    @Mapping(source = "userDetail.mapLocation", target = "mapLocation")
    @Mapping(source = "userDetail.description", target = "description")
    @Mapping(source = "userDetail.resumeUrl", target = "resumeUrl")
    @Mapping(source = "verified", target = "verified")
    @Mapping(source = "isPremium", target = "isPremium")
    @Mapping(source = "userDetail.avatarUrl", target = "avatarUrl")
    @Mapping(target = "organization", ignore = true) // Will be handled separately
    @Mapping(target = "education", ignore = true) // Will be handled separately
    @Mapping(target = "experience", ignore = true) // Will be handled separately
    @Mapping(target = "totalJobsPosted", ignore = true) // Calculated field
    @Mapping(target = "totalApplications", ignore = true) // Calculated field
    @Mapping(target = "averageRating", ignore = true) // Calculated field
    @Mapping(target = "totalReviews", ignore = true) // Calculated field
    UserResponse toUserResponse(User user);

    // --- Mapper để cập nhật User Entity từ Update Request DTO ---
    // @MappingTarget chỉ định đối tượng User hiện có sẽ được cập nhật
    // nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE sẽ giúp bỏ qua các trường null trong request
    @Mapping(target = "id", ignore = true) // Không cập nhật ID
    @Mapping(target = "password", ignore = true) // Không cập nhật mật khẩu ở đây
    @Mapping(target = "role", ignore = true) // Role được xử lý logic riêng trong Service
    @Mapping(target = "userDetail", ignore = true) // UserDetail được xử lý logic riêng trong Service
    @Mapping(target = "createdAt", ignore = true) // Không cập nhật thời gian tạo
    @Mapping(target = "updatedAt", ignore = true) // Thời gian cập nhật được DB hoặc Entity Listener tự động set
    // Sử dụng default method map(Boolean) để chuyển đổi từ Boolean sang Integer (0/1) cho 'verified'
    @Mapping(source = "isPremium", target = "isPremium")
    @Mapping(source = "verified", target = "verified") // Sẽ dùng default map(Boolean value)
    @Mapping(target = "resetPasswordExpiry", ignore = true)
    @Mapping(target = "resetPasswordToken", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "chatbotHistories", ignore = true)
    @Mapping(target = "jobRecommendations", ignore = true)
    @Mapping(target = "jobViews", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "postedJobs", ignore = true)
    @Mapping(target = "reviewsGiven", ignore = true)
    @Mapping(target = "reviewsReceived", ignore = true)
    @Mapping(target = "savedJobs", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    List<UserResponse> toUserResponseList(List<User> users);

    // --- Mapper từ UserDocument sang UserResponse ---
    @Mapping(source = "id", target = "id")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "location", target = "location")
    @Mapping(source = "companyName", target = "companyName")
    @Mapping(source = "website", target = "website")
    @Mapping(source = "teamSize", target = "teamSize")
    @Mapping(source = "yearOfEstablishment", target = "yearOfEstablishment")
    @Mapping(source = "mapLocation", target = "mapLocation")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "jobsPosted", target = "totalJobsPosted", qualifiedByName = "integerToLong")
    @Mapping(source = "averageRating", target = "averageRating")
    @Mapping(source = "totalReviews", target = "totalReviews")
    @Mapping(source = "isPremium", target = "isPremium")
    @Mapping(source = "verified", target = "verified")
    @Mapping(source = "avatarUrl", target = "avatarUrl")
    @Mapping(source = "banner", target = "banner")
    @Mapping(target = "role", ignore = true) // Will be set by UserDocumentMapper
    @Mapping(target = "createdAt", ignore = true) // String to LocalDateTime conversion needed
    @Mapping(target = "updatedAt", ignore = true) // String to LocalDateTime conversion needed
    @Mapping(target = "organization", ignore = true) // Will be set by UserDocumentMapper
    @Mapping(target = "resumeUrl", ignore = true) // Not available in UserDocument for employers
    @Mapping(target = "education", ignore = true) // Will be set by UserDocumentMapper
    @Mapping(target = "experience", ignore = true) // Will be set by UserDocumentMapper
    @Mapping(target = "totalApplications", ignore = true) // Not available in UserDocument
    UserResponse toUserResponse(UserDocument userDocument);

    @Mapping(source = "role.name", target = "roleName")
    UserDto toUserDto(User user);

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

    default Long mapIntegerToLong(Integer value) {
        return value != null ? value.longValue() : null;
    }

    @org.mapstruct.Named("integerToLong")
    default Long integerToLong(Integer value) {
        return value != null ? value.longValue() : null;
    }
}