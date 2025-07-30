package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_views",
        indexes = {@Index(name = "idx_job_seeker_job", columnList = "job_id, job_seeker_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "viewed_at", nullable = false, updatable = false)
    private LocalDateTime viewedAt;

    // --- Mối quan hệ ---

    // Một JobView liên quan đến một Job
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonManagedReference("job-jobviews")
    private Job job;

    // Một JobView được thực hiện bởi một Job Seeker (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    @JsonManagedReference("jobseeker-jobviews")
    private User jobSeeker;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.viewedAt = LocalDateTime.now();
    }
}