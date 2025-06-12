package com.example.jobfinder.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "user_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDetails { // Đổi tên lớp thành UserDetails thay vì UserDetail để khớp tốt hơn
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY) // Mối quan hệ 1-1 với User
    @JoinColumn(name = "user_id", nullable = false) // user_id không null
    User user;

    @Column(length = 255)
    String location;

    @Column(length = 255)
    String fullName;

    @Column(length = 50)
    String phone;

    @Column(name = "years_experience")
    Integer yearsExperience; // Dùng Integer để có thể null

    @Column(name = "resume_url", columnDefinition = "TEXT") // Sử dụng columnDefinition cho TEXT
    String resumeUrl;

    @Column(name = "company_name", length = 255)
    String companyName;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(length = 255)
    String website;

    @ManyToOne(fetch = FetchType.LAZY) // Mối quan hệ N-1 với Education
    @JoinColumn(name = "education_id") // education_id có thể null
    Education education;
}