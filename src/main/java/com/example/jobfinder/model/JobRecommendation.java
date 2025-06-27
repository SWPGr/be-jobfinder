package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Float score;

    @Column(name = "recommended_at", nullable = false, updatable = false)
    private LocalDateTime recommendedAt;

    // --- Mối quan hệ ---

    // Một JobRecommendation thuộc về một Job Seeker (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    @JsonManagedReference("jobseeker-recommendations")
    private User jobSeeker;

    // Một JobRecommendation liên quan đến một Job
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonManagedReference("job-recommendations")
    private Job job;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.recommendedAt = LocalDateTime.now();
    }
}