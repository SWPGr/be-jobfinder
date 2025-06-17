package com.example.jobfinder.model;

import com.example.jobfinder.model.enums.ApplicationStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"job_seeker_id", "job_id"}) // Ánh xạ UNIQUE constraint từ DB
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationStatus status;

    @Column(name = "applied_at", nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    // --- Mối quan hệ ---

    // Một Application thuộc về một Job Seeker (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    @JsonManagedReference("jobseeker-applications")
    private User jobSeeker;

    // Một Application liên quan đến một Job
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonManagedReference("job-applications")
    private Job job;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.appliedAt = LocalDateTime.now();
    }
}