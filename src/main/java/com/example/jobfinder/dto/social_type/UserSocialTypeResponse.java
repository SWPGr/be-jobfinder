package com.example.jobfinder.dto.social_type;

// import com.example.jobfinder.dto.user_detail.UserDetailResponse; // Tùy chọn nếu muốn trả về UserDetail đầy đủ
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSocialTypeResponse {
    Long id;
    SimpleNameResponse socialType; // Loại mạng xã hội (id, name)
    String url;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    // Bỏ qua UserDetailResponse để tránh lặp hoặc dữ liệu thừa
}