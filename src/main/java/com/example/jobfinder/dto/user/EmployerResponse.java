// src/main/java/com/example/jobfinder/dto/employer/EmployerResponse.java
package com.example.jobfinder.dto.user;
import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerResponse {
    private Long userId; // ID của UserDetail
    private String userEmail; // Email của User (từ User)
    private String fullName;  // Từ UserDetail
    private String phone;     // Từ UserDetail
    private String location;  // Từ UserDetail

    // Các trường dành riêng cho Employer từ UserDetail
    private String companyName;
    private String description; // Mô tả công ty
    private String website;
}