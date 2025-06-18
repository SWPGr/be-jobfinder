
package com.example.jobfinder.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @Email(message = "USER_EMAIL_INVALID")
    String email; // Có thể để null nếu không muốn cập nhật email

    @Size(min = 6, message = "USER_PASSWORD_MIN_LENGTH")
    String password; // Có thể để null nếu không muốn cập nhật password

    Long roleId; // Có thể để null nếu không muốn cập nhật role
}