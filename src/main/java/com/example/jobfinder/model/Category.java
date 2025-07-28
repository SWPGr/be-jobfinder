package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*; // Đảm bảo import AccessLevel
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
@Setter
@SuperBuilder
@ToString(callSuper = true)
@AttributeOverride(name = "name", column = @Column(name = "category_name", unique = true, nullable = false, length = 100))
public class Category extends BaseNameEntity {
    public Category() {

    }

    // Một Category có thể có nhiều Job
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("category-jobs")
    private Set<Job> jobs = new HashSet<>();

    @OneToMany
    @JsonBackReference("category-userDetails")
    @JsonIgnore
    private Set<UserDetail> users = new HashSet<>();
}