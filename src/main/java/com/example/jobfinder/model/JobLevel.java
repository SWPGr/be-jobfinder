package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "job_levels")
@Setter
@Getter
@SuperBuilder
@ToString(callSuper = true)
@AttributeOverride(name = "name", column = @Column(name = "job_level_name", unique = true, nullable = false, length = 50))
public class JobLevel extends BaseNameEntity {
    public JobLevel() {

    }

    // Một JobLevel có thể có nhiều Job
    @OneToMany(mappedBy = "jobLevel", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("jobLevel-jobs")
    private Set<Job> jobs = new HashSet<>();
}