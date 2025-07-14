package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(name = "salary_min")
    private Float salaryMin;

    @Column(name = "salary_max")
    private Float salaryMax;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDate expiredDate;

    private Integer vacancy;

    @Column(columnDefinition = "TEXT")
    private String responsibility;

    private boolean isSave;

    @Column(nullable = false)
    private Boolean active = true;

    // --- Mối quan hệ ---

    // Một Job được đăng bởi một Employer (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    @JsonManagedReference("employer-jobs")
    private User employer;

    // Một Job thuộc về một Category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonManagedReference("category-jobs")
    private Category category;

    // Một Job có JobLevel
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_level_id") // job_level_id có thể NULL
    @JsonManagedReference("jobLevel-jobs")
    private JobLevel jobLevel;

    // Một Job có JobType
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_type_id") // job_type_id có thể NULL
    @JsonManagedReference("jobType-jobs")
    private JobType jobType;

    // Một Job có nhiều Application
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("job-applications")
    private Set<Application> applications = new HashSet<>();

    // Một Job có nhiều JobRecommendation
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("job-recommendations")
    private Set<JobRecommendation> jobRecommendations = new HashSet<>();

    // Một Job có nhiều SavedJob
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("job-savedjobs")
    private Set<SavedJob> savedJobs = new HashSet<>();

    // Một Job có nhiều JobView
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("job-jobviews")
    private Set<JobView> jobViews = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "job_education_id")
    private Education education;

    @ManyToOne
    @JoinColumn(name = "job_experience_id")
    private Experience experience;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}