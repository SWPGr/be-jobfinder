package com.example.jobfinder.dto.social_type;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSocialTypeRequest {
    @NotNull(message = "Social type ID must not be null")
    Long socialTypeId; // ID của loại mạng xã hội (e.g., 1 for Facebook)

    @NotBlank(message = "URL cannot be blank")
    @URL(message = "Invalid URL format") // Kích hoạt validation URL
    String url;
}