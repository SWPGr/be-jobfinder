package com.example.jobfinder.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Getter
@Setter
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employer_id", nullable = false)
    private User employerId;
    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column
    private String location;
    @Column(name = "salary_min")
    private Float salaryMin;
    @Column(name = "salary_max")
    private Float salaryMax;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @ManyToOne
    @JoinColumn(name = "job_level_id", nullable = false)
    private JobLevel jobLevel;
    @ManyToOne
    @JoinColumn(name = "job_type_id", nullable = false)
    private JobType jobType;
}
