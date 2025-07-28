package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "job_types")
@Setter
@Getter
@SuperBuilder
@ToString(callSuper = true)
@AttributeOverride(name = "name", column = @Column(name = "job_type_name", unique = true, nullable = false, length = 50))
public class JobType extends BaseNameEntity {
    public JobType() {

    }
    // Một JobType có thể có nhiều Job
    @OneToMany(mappedBy = "jobType", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("jobType-jobs")
    @JsonIgnore
    private Set<Job> jobs = new HashSet<>();
}