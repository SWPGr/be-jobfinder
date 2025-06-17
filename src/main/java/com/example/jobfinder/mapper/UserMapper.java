package com.example.jobfinder.mapper;

import com.example.jobfinder.model.User;
import com.example.jobfinder.dto.user.UserResponse; // Giả định DTO này có trường Boolean verified
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants; // Thêm import này
import org.mapstruct.NullValuePropertyMappingStrategy; // Thêm import này

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, // Sử dụng Spring component model
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE) // Bỏ qua các trường null khi map
public interface UserMapper {

    // Phương thức để map từ User entity sang UserResponse DTO
    @Mapping(source = "role", target = "role")
    @Mapping(source = "isPremium", target = "isPremium")
    // Không cần @Mapping cho 'verified' nếu bạn dùng phương thức map()
    UserResponse toUserResponse(User user);

    // Phương thức để map từ UserResponse DTO sang User entity
    @Mapping(source = "role", target = "role") // Đây có thể là một vấn đề nếu Role không tồn tại
    @Mapping(source = "isPremium", target = "isPremium")
    User toUser(UserResponse userResponse); // Thường là UserRequest thay vì UserResponse cho entity tạo mới/cập nhật

    List<UserResponse> toUserResponseList(List<User> users);

    default Boolean map(Integer value) {
        if (value == null) {
            return null; // Or false, depending on your default behavior for null Integer
        }
        return value != 0; // If 0, then false. If 1 or any other non-zero, then true.
    }

    default Integer map(Boolean value) {
        if (value == null) {
            return null; // Or 0, depending on your default behavior for null Boolean
        }
        return value ? 1 : 0; // If true, then 1. If false, then 0.
    }
}