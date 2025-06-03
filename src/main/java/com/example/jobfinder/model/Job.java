package com.example.jobfinder.model;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.annotations.CreationTimestamp; // Import này quan trọng
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class) // Kích hoạt Auditing cho Entity này
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Mối quan hệ N-1 với User (employer)
    @JoinColumn(name = "employer_id", nullable = false) // employer_id không null
    User employer;

    @Column(nullable = false, length = 255)
    String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    String description;

    @Column(nullable = false, length = 255)
    String location;

    @Column(name = "salary_min", nullable = false)
    Float salaryMin;

    @Column(name = "salary_max", nullable = false)
    Float salaryMax;

    @CreationTimestamp // Tự động điền khi tạo
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY) // Mối quan hệ N-1 với Category
    @JoinColumn(name = "category_id", nullable = false) // category_id không null
    Category category;

    @ManyToOne(fetch = FetchType.LAZY) // Mối quan hệ N-1 với JobLevel
    @JoinColumn(name = "job_level_id") // job_level_id có thể null
    JobLevel jobLevel; // Đổi tên từ job_level_id sang jobLevel

    @ManyToOne(fetch = FetchType.LAZY) // Mối quan hệ N-1 với JobType
    @JoinColumn(name = "job_type_id") // job_type_id có thể null
    JobType jobType; // Đổi tên từ job_type_id sang jobType

    // Mối quan hệ 1-n với Applications
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Set<Application> applications;

    // Mối quan hệ 1-n với JobRecommendations
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Set<JobRecommendation> jobRecommendations;

    // Mối quan hệ 1-n với SavedJobs
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Set<SavedJob> savedJobs;

    // Mối quan hệ 1-n với JobViews
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Set<JobView> jobViews;
}