package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "educations") // Tên bảng trong cơ sở dữ liệu
@Setter
@Getter
@SuperBuilder
@ToString(callSuper = true)
@AttributeOverride(name = "name", column = @Column(name = "education_name", unique = true, nullable = false, length = 100))
public class Education extends BaseNameEntity {
    public Education() {}

    // Một Education có thể có nhiều UserDetail
    @OneToMany(mappedBy = "education", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("education-userDetails")
    private Set<UserDetail> userDetails = new HashSet<>();

}