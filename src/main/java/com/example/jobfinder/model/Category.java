package com.example.jobfinder.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*; // Đảm bảo import AccessLevel
import lombok.experimental.SuperBuilder;

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
}