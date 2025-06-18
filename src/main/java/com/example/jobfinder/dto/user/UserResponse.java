
package com.example.jobfinder.dto.user;

import com.example.jobfinder.dto.simple.SimpleNameResponse; // <-- Import RoleResponse
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
    Boolean isPremium;

    SimpleNameResponse role; // <-- Trả về đối tượng RoleResponse lồng nhau
}