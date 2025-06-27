package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "jobs")
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

    @Column(name = "salary_min", nullable = false)
    private Float salaryMin;

    @Column(name = "salary_max", nullable = false)
    private Float salaryMax;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Float getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(Float salaryMin) {
        this.salaryMin = salaryMin;
    }

    public Float getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(Float salaryMax) {
        this.salaryMax = salaryMax;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getEmployer() {
        return employer;
    }

    public void setEmployer(User employer) {
        this.employer = employer;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public JobLevel getJobLevel() {
        return jobLevel;
    }

    public void setJobLevel(JobLevel jobLevel) {
        this.jobLevel = jobLevel;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public Set<Application> getApplications() {
        return applications;
    }

    public void setApplications(Set<Application> applications) {
        this.applications = applications;
    }

    public Set<JobRecommendation> getJobRecommendations() {
        return jobRecommendations;
    }

    public void setJobRecommendations(Set<JobRecommendation> jobRecommendations) {
        this.jobRecommendations = jobRecommendations;
    }

    public Set<SavedJob> getSavedJobs() {
        return savedJobs;
    }

    public void setSavedJobs(Set<SavedJob> savedJobs) {
        this.savedJobs = savedJobs;
    }

    public Set<JobView> getJobViews() {
        return jobViews;
    }

    public void setJobViews(Set<JobView> jobViews) {
        this.jobViews = jobViews;
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}