package com.example.jobfinder.dto;
import com.example.jobfinder.model.Category;
import com.example.jobfinder.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobResponse {
    Long id;
    User employer;
    String title;
    String description;
    String location;
    Float salaryMin;
    Float salaryMax;
    Long employerId;
    LocalDateTime createdAt;
    Category category;
}
