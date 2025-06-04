package com.example.jobfinder.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "educations") // Tên bảng trong cơ sở dữ liệu
@Setter
@Getter
@SuperBuilder
@ToString(callSuper = true)
@AttributeOverride(name = "name", column = @Column(name = "education_name", unique = true, nullable = false, length = 100))
public class Education extends BaseNameEntity {
    public Education() {

    }
}