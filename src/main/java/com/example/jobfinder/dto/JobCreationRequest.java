package com.example.jobfinder.dto;
import com.example.jobfinder.model.Category;
import com.example.jobfinder.model.User;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobCreationRequest {
    Long id;
    User employer;
    String title;
    String description;
    String location;
    @Min(value = 0, message = "SALARY_MIN_INVALID")
    Float salaryMin;
    @Min(value = 0, message = "SALARY_MIN_INVALID")
    Float salaryMax;
    LocalDateTime createdAt;
    Category category;
}
