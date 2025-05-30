package com.example.jobfinder.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;


@Data
@Table(name="job")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
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

    // @CreatedDate // Bỏ comment nếu bạn dùng JPA Auditing
    @Column(name = "created_at", nullable = false) // updatable = false nếu dùng @CreatedDate
     LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
     Category category;
}
