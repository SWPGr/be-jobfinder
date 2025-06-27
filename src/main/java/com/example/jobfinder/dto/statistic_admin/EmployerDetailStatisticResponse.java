// src/main/java/com/example/jobfinder/dto/response/UserDetailStatisticResponse.java
package com.example.jobfinder.dto.statistic_admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // Quan trọng cho Builder và constructor trong @Query
@Builder
public class EmployerDetailStatisticResponse {
    private Long userId; // ID của UserDetail
    private String userEmail; // Email của User (từ User)
    private String fullName;  // Từ UserDetail
    private String phone;     // Từ UserDetail
    private String location;  // Từ UserDetail

    // Các trường dành riêng cho Employer từ UserDetail
    private String companyName;
    private String description; // Mô tả công ty
    private String website;

    private Long totalJob;
}