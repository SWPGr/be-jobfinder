// src/main/java/com/example/jobfinder/dto/user/UserSearchRequest.java
package com.example.jobfinder.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


//UserSearchRequest request = UserSearchRequest.builder()
//        .roleName("JOB_SEEKER")
//        .location("Hanoi")
//        .yearsExperience(3)
//        .build();
@Data // Tự động tạo getters, setters, toString, equals, hashCode
@NoArgsConstructor // Tự động tạo constructor không đối số
@AllArgsConstructor // Tự động tạo constructor với tất cả các đối số
@Builder // Tự động tạo builder pattern cho việc khởi tạo đối tượng dễ dàng hơn
public class UserSearchRequest {
    // Các tiêu chí tìm kiếm chung từ User entity
    private String email; // Tìm kiếm gần đúng (LIKE %value%) theo email của người dùng.
    private Boolean isPremium; // Lọc theo trạng thái tài khoản premium (true/false).
    private Integer verified; // Lọc theo trạng thái xác minh tài khoản (0: chưa xác minh, 1: đã xác minh).

    // Các tiêu chí tìm kiếm từ UserDetail entity (áp dụng cho cả JobSeeker và Employer)
    private String fullName; // Tìm kiếm gần đúng theo tên đầy đủ của người dùng.
    private String phone;    // Tìm kiếm gần đúng theo số điện thoại.
    private String location; // Tìm kiếm gần đúng theo địa điểm.

    // Các tiêu chí tìm kiếm chuyên biệt cho JobSeeker (sẽ được bỏ qua nếu vai trò không phải JOB_SEEKER)
    private Integer yearsExperience; // Lọc theo số năm kinh nghiệm chính xác.
    private String resumeUrl; // Dùng để kiểm tra xem JobSeeker có resume hay không (IS NOT NULL).
    // Khi tìm kiếm, nếu trường này không null, truy vấn sẽ lọc những người có resume.
    private Long educationId; // Lọc theo ID của thông tin giáo dục.

    // Các tiêu chí tìm kiếm chuyên biệt cho Employer (sẽ được bỏ qua nếu vai trò không phải EMPLOYER)
    private String companyName; // Tìm kiếm gần đúng theo tên công ty.
    private String description; // Tìm kiếm gần đúng theo mô tả công ty.
    private String website;     // Dùng để kiểm tra xem Employer có website hay không (IS NOT NULL).
    // Tương tự resumeUrl, nếu trường này không null, truy vấn sẽ lọc những người có website.

    // Tiêu chí tìm kiếm theo vai trò (đây là một tiêu chí quan trọng để phân biệt JobSeeker/Employer/Admin)
    private String roleName; // Tên vai trò (ví dụ: "JOB_SEEKER", "EMPLOYER", "ADMIN").
    // Khi được cung cấp, sẽ lọc người dùng theo vai trò cụ thể.
}