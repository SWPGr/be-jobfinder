// src/main/java/com/example/jobfinder/model/ResumeSummary.java
package com.example.jobfinder.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resume_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với đơn ứng tuyển (Application)
    // Một đơn ứng tuyển chỉ có một bản tóm tắt duy nhất (hoặc bản tóm tắt gần nhất)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", unique = true, nullable = false) // unique = true đảm bảo 1-1
    private Application application;

    @Column(name = "summary_content", columnDefinition = "TEXT", nullable = false)
    private String summaryContent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Để theo dõi khi bản tóm tắt được cập nhật (nếu có)

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}