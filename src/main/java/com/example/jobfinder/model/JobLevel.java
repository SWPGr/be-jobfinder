package com.example.jobfinder.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "job_levels")
@Setter
@Getter
@SuperBuilder
@ToString(callSuper = true)
@AttributeOverride(name = "name", column = @Column(name = "level_name", unique = true, nullable = false, length = 50))
public class JobLevel extends BaseNameEntity {
    public JobLevel() {

    }
    // ...
}