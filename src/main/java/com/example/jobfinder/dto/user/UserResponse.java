
package com.example.jobfinder.dto.user;

import lombok.*;
import lombok.experimental.FieldDefaults;


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
    private String roleName;  // To hold role.name
    private String fullName;  // To hold userDetail.fullName
    private String phone;     // To hold userDetail.phone
    private String location;  // To hold userDetail.location
    private Integer verified;
    private String avatarUrl;

    private String companyName;
    private String website;
    private Long totalJobsPosted;
}