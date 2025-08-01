package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.UpdateTimestamp;

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
@Filter(name = "activeRelatedUserFilter", condition = "employer_id IN (SELECT u.id FROM users u WHERE u.active = :isActive)")
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

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "expired_date")
    private LocalDate expiredDate;

    @Column(name = "vacancy")
    private Integer vacancy;

    @Column(columnDefinition = "TEXT")
    private String responsibility;

    @Column(name = "is_save")
    private boolean isSave;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    @JsonManagedReference("employer-jobs")
    private User employer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonManagedReference("category-jobs")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_level_id")
    @JsonManagedReference("jobLevel-jobs")
    private JobLevel jobLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_type_id")
    @JsonManagedReference("jobType-jobs")
    private JobType jobType;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("job-applications")
    private Set<Application> applications = new HashSet<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("job-recommendations")
    private Set<JobRecommendation> jobRecommendations = new HashSet<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("job-savedjobs")
    private Set<SavedJob> savedJobs = new HashSet<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("job-jobviews")
    private Set<JobView> jobViews = new HashSet<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.DETACH, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("job-notifications")
    private Set<Notification> notifications = new HashSet<>();


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
        if (active == null) {
            active = true; // Đảm bảo giá trị mặc định cho active khi tạo mới
        }
    }
}