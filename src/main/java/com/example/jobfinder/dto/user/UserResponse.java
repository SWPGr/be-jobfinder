
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
    private Long id;
    private String email;
    private Boolean isPremium; // Changed from Integer to Boolean for better representation
    private String createdAt; // Or LocalDateTime, depending on your preference
    private String updatedAt; // Or LocalDateTime

    // New fields to be mapped from Role and UserDetail
    private String roleName;
    private String fullName;
    private String phone;
    private String location;
    private Integer verified;

    private String companyName;
    private String website;
    private Long totalJobsPosted;
}