package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_jobs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"job_seeker_id", "job_id"}) // Ánh xạ UNIQUE constraint từ DB
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    // --- Mối quan hệ ---

    // Một SavedJob thuộc về một Job Seeker (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    @JsonManagedReference("jobseeker-savedjobs")
    private User jobSeeker;

    // Một SavedJob liên quan đến một Job
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonManagedReference("job-savedjobs")
    private Job job;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.savedAt = LocalDateTime.now();
    }
}