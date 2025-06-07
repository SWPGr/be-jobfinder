// D:\Code-Window\JobFinderProject\be-jobfinder\trunglecode\src\main\java\com\example\jobfinder\mapper\UserMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.user.UserResponse; // Import UserResponse
import com.example.jobfinder.model.User; // Import User entity
import com.example.jobfinder.model.Role; // <-- Import Role Entity (nếu bạn có)
import com.example.jobfinder.dto.SimpleNameResponse; // <-- Import SimpleNameResponse

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = {SimpleNameMapper.class}) // <-- Rất quan trọng: UserMapper sử dụng SimpleNameMapper
// để ánh xạ trường 'role'
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "verified", source = "verified")
    @Mapping(target = "role", source = "role")
    UserResponse toUserResponse(User user);

    // Bạn cũng có thể thêm một phương thức để ánh xạ Role Entity sang SimpleNameResponse
    // nếu bạn không muốn SimpleNameMapper xử lý Role, nhưng dùng SimpleNameMapper là tốt hơn
    // SimpleNameResponse toSimpleNameResponse(Role role); // Có thể đặt ở đây hoặc trong SimpleNameMapper
}