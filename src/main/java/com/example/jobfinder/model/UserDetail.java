package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String location;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(length = 50)
    private String phone;

    @Column(name = "resume_url", columnDefinition = "TEXT")
    private String resumeUrl;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String website;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "banner", length = 512)
    private String banner;

    @Column(name = "team_size", length = 100)
    private String teamSize;

    @Column(name = "year_of_establishment")
    private Integer yearOfEstablishment;

    @Column(name = "map_location", length = 255)
    private String mapLocation;

    @Column(name = "company_vision", columnDefinition = "TEXT")
    private String companyVision;
  

    // --- Mối quan hệ ---

    // Một UserDetail thuộc về một User (OneToOne)
    // UserDetail sở hữu khóa ngoại user_id
    // @MapsId được sử dụng nếu khóa chính của UserDetail cũng là khóa ngoại đến User.
    // Nếu id của UserDetail không phải là id của User, thì sử dụng @JoinColumn.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @JsonManagedReference("user-userDetail") // Phía sở hữu khóa ngoại là ManagedReference
    private User user;

    // Một UserDetail có một Education
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "education_id") // education_id có thể NULL theo DB schema
    @JsonManagedReference("education-userDetails")
    private Education education;

    // Một UserDetail có nhiều UserSocialType
    @OneToMany(mappedBy = "userDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("userDetail-userSocialTypes")
    private Set<UserSocialType> userSocialTypes = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_experience_id")
    private Experience experience;

    @ManyToOne
    @JoinColumn(name = "user_organization_type")
    @JsonBackReference("organization-userDetails")
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonBackReference("category-userDetails")
    private Category category;
}