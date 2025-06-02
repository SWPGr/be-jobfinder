package com.example.jobfinder.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_details")
@Getter
@Setter
public class UserDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "education_id")
    private Education education;

    @Column(length = 255)
    private String location;
    @Column(length = 255)
    private String fullName;
    @Column(length = 50)
    private String phone;
    @Column(name = "years_experience")
    private Integer yearsExperience;
    @Column(name = "resume_url")
    private String resumeUrl;
    @Column(name = "company_name", length = 255)
    private String companyName;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(length = 255)
    private String website;
}
