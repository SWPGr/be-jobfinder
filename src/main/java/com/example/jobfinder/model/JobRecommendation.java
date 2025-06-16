package com.example.jobfinder.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_recommendations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"job_seeker_id", "job_id"})
})
@Getter
@Setter
public class JobRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_seeker_id", nullable = false)
    private User jobSeeker;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false)
    private float score;

    @Column(name = "recommended_at")
    private LocalDateTime recommendedAt;
}
