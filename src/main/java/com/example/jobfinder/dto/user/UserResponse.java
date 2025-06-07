
package com.example.jobfinder.dto.user;

import com.example.jobfinder.dto.SimpleNameResponse; // <-- Import RoleResponse
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String email;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Boolean verified;

    SimpleNameResponse role; // <-- Trả về đối tượng RoleResponse lồng nhau
}