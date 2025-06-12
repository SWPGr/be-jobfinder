// D:\Code-Window\JobFinderProject\be-jobfinder\trunglecode\src\main\java\com\example\jobfinder\model\JobType.java
package com.example.jobfinder.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "job_types")
@Setter
@Getter
@SuperBuilder
@ToString(callSuper = true)
@AttributeOverride(name = "name", column = @Column(name = "type_name", unique = true, nullable = false, length = 50))
public class JobType extends BaseNameEntity {
    public JobType() {

    }
    // Không cần khai báo lại thuộc tính
}