    package com.example.jobfinder.dto.employer_review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployerReviewRequest {

    @NotNull(message = "Employer ID must not be null")
    private Long employerId;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not be more than 5")
    @NotNull(message = "Rating must not be null")
    private Integer rating;

    @NotBlank(message = "Comment must not be blank")
    private String comment;
}