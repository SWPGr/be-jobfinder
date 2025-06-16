package com.example.jobfinder.model;


import com.example.jobfinder.model.enums.ApplicationStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"job_seeker_id", "job_id"})
})
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    @JsonBackReference
    User jobSeeker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonBackReference
    Job job;

    @Enumerated(EnumType.STRING) // <-- Quan trọng: Lưu trữ tên enum dưới dạng String trong DB
    @Column(name = "status", nullable = false)
    private ApplicationStatus status; ; // e.g., "Pending", "Reviewed", "Accepted", "Rejected"

    @CreatedDate
    @Column(name = "applied_at", nullable = false, updatable = false)
    LocalDateTime appliedAt;
}