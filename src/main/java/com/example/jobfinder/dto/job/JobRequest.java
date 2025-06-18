package com.example.jobfinder.dto.job;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class JobRequest {
    private Long employerId;
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;
    @NotBlank(message = "Description is required")
    private String description;
    private String location;
    @DecimalMin(value = "0.0", message = "Salary must be positive")
    private Float salaryMin;
    private Float salaryMax;
    @NotNull(message = "Category is required")
    private Long categoryId;
    private Long jobLevelId;
    private Long jobTypeId;
}
