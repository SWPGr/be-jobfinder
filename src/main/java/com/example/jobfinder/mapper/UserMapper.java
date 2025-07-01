// src/main/java/com/example/jobfinder/mapper/UserMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.model.User;
import com.example.jobfinder.model.UserDetail; // Cần import UserDetail
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.dto.user.UserCreationRequest; // Cần import UserCreationRequest
import com.example.jobfinder.dto.user.UserUpdateRequest; // Cần import UserUpdateRequest
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
    User toUser(UserCreationRequest request);

    // --- Mapper từ Entity sang Response DTO để hiển thị thông tin User ---
    @Mapping(source = "role.name", target = "roleName")
    @Mapping(source = "userDetail.fullName", target = "fullName")
    @Mapping(source = "userDetail.phone", target = "phone")
    @Mapping(source = "userDetail.location", target = "location")
    @Mapping(source = "verified", target = "verified") // Will use default map(Integer value)
    // Make sure 'enabled' and 'isPremium' also have direct mappings or setters if they are in UserResponse
    @Mapping(source = "isPremium", target = "isPremium")
    @Mapping(source = "userDetail.avatarUrl", target = "avatarUrl")
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
}