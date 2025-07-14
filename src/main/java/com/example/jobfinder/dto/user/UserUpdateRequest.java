// src/main/java/com/example/jobfinder/dto/user/UserUpdateRequest.java
package com.example.jobfinder.dto.user;

import lombok.*;
@Data // Tự động tạo getters, setters, toString, equals, hashCode
@NoArgsConstructor // Tự động tạo constructor không đối số
@AllArgsConstructor // Tự động tạo constructor với tất cả các đối số
@Builder // Tự động tạo builder pattern cho việc khởi tạo đối tượng dễ dàng hơn
public class UserUpdateRequest {
    private String email;
    private String roleName;
    private String fullName;
    private String phone;
    private String location;

    private Boolean enabled;   // This field likely needs to be Boolean for updating
    private Boolean isPremium; // Add this field if it's not there, type Boolean
    private Boolean verified;  // Add this field if it's not there, type Boolean (will be mapped to Integer in User)

    // ... job seeker/employer specific fields ...
    private Long userExperience;
    private String resumeUrl;
    private Long educationId;
    private String companyName;
    private String description;
    private String website;

    //các trường được bổ sung tiếp theo
    private String banner; // URL của banner
    private String teamSize; // Kích thước đội ngũ (ví dụ: "1-10", "11-50", "50+")
    private Integer yearOfEstablishment; // Năm thành lập
    private String mapLocation; // Vị trí trên bản đồ (ví dụ: tọa độ, hoặc link Google Maps)
    private String organizationType; // Loại hình tổ chức (ví dụ: "Startup", "SME", "Corporation")
    private String avatarUrl; // URL của ảnh đại diện công ty
}