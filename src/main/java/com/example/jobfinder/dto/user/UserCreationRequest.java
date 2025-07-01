// src/main/java/com/example/jobfinder/dto/user/UserCreationRequest.java
package com.example.jobfinder.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO (Data Transfer Object) để đóng gói thông tin cần thiết
 * khi tạo một người dùng mới trong hệ thống.
 * Bao gồm các trường cho cả User entity và UserDetail entity,
 * với các thuộc tính chuyên biệt cho JobSeeker và Employer.
 */
@Data // Tự động tạo getters, setters, toString, equals, hashCode
@NoArgsConstructor // Tự động tạo constructor không đối số
@AllArgsConstructor // Tự động tạo constructor với tất cả các đối số
@Builder // Tự động tạo builder pattern cho việc khởi tạo đối tượng dễ dàng hơn
public class UserCreationRequest {

    // --- Thông tin bắt buộc cho User entity ---
    @NotBlank(message = "Email không được để trống") // Đảm bảo trường không rỗng và không chỉ chứa khoảng trắng
    @Email(message = "Email không hợp lệ") // Đảm bảo định dạng email hợp lệ
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự") // Đảm bảo độ dài tối thiểu cho mật khẩu
    private String password;

    @NotBlank(message = "Tên vai trò không được để trống") // Ví dụ: "ADMIN", "JOB_SEEKER", "EMPLOYER"
    private String roleName;

    // --- Thông tin chung cho UserDetail entity (áp dụng cho mọi vai trò có UserDetail) ---
    @NotBlank(message = "Tên đầy đủ không được để trống")
    private String fullName;

    @Size(max = 50, message = "Số điện thoại không được vượt quá 50 ký tự")
    private String phone;

    @Size(max = 255, message = "Địa điểm không được vượt quá 255 ký tự")
    private String location;

    // --- Thông tin chuyên biệt cho vai trò JOB_SEEKER (sẽ là null nếu roleName không phải JOB_SEEKER) ---
    private Long userExperience; // Số năm kinh nghiệm, có thể null nếu không phải JobSeeker hoặc chưa có
    private String resumeUrl; // URL của CV, có thể null nếu không phải JobSeeker hoặc chưa có
    private Long educationId; // ID của thông tin giáo dục liên quan, có thể null nếu không có hoặc không phải JobSeeker

    // --- Thông tin chuyên biệt cho vai trò EMPLOYER (sẽ là null nếu roleName không phải EMPLOYER) ---
    @Size(max = 255, message = "Tên công ty không được vượt quá 255 ký tự")
    private String companyName; // Tên công ty, có thể null nếu không phải Employer
    private String description; // Mô tả công ty, có thể null nếu không phải Employer
    @Size(max = 255, message = "Website không được vượt quá 255 ký tự")
    private String website;     // Website của công ty, có thể null nếu không phải Employer

    private Integer verified;
    private Boolean isPremium;
}