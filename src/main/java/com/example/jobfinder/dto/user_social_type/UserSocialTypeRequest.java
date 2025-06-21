// src/main/java/com/example/jobfinder/dto/user_social_type/UserSocialTypeRequest.java
package com.example.jobfinder.dto.user_social_type;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSocialTypeRequest {

    @NotNull(message = "ID loại mạng xã hội không được để trống")
    private Long socialTypeId;

    @NotBlank(message = "URL không được để trống")
    @Size(max = 255, message = "URL không được vượt quá 255 ký tự")
    // @URL(message = "URL không hợp lệ") // Có thể thêm nếu bạn dùng thư viện Hibernate Validator Extras
    private String url;
}