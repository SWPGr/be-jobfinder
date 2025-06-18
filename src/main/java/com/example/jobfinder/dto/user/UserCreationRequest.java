package com.example.jobfinder.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank(message = "USER_EMAIL_BLANK")
    @Email(message = "USER_EMAIL_INVALID")
    String email;

    @NotBlank(message = "USER_PASSWORD_BLANK")
    @Size(min = 6, message = "USER_PASSWORD_MIN_LENGTH")
    String password;

    Long roleId; // ID của Role
}