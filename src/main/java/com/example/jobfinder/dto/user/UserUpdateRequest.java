
package com.example.jobfinder.dto.user;
import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    private String email;
    private String roleName;
    private String fullName;
    private String phone;
    private String location;

    private Boolean enabled;
    private Boolean isPremium;
    private Boolean verified;

    // ... job seeker/employer specific fields ...
    private Long userExperience;
    private String resumeUrl;
    private Long educationId;
    private String companyName;
    private String description;
    private String website;

    //các trường được bổ sung tiếp theo
    private String banner;
    private String teamSize;
    private Integer yearOfEstablishment; // Năm thành lập
    private String mapLocation; // Vị trí trên bản đồ (ví dụ: tọa độ, hoặc link Google Maps)
    private String organizationType; // Loại hình tổ chức (ví dụ: "Startup", "SME", "Corporation")
    private String avatarUrl; // URL của ảnh đại diện công ty
}