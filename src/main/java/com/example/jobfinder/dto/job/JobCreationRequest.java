package com.example.jobfinder.dto.job;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

// Không cần import Category và User nếu bạn chỉ dùng ID của chúng

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobCreationRequest {


    @NotBlank(message = "TITLE_REQUIRED")
    String title;

    @NotBlank(message = "DESCRIPTION_REQUIRED")
    String description;

    @Min(value = 0, message = "SALARY_MIN_INVALID")
    Float salaryMin;

    // Có thể thêm @NotNull nếu salaryMax là bắt buộc
    @Min(value = 0, message = "SALARY_MAX_INVALID")
    Float salaryMax;
    LocalDate expiredDate;
    Integer vacancy;
    String responsibility;

    // Không cần trường 'createdAt' vì nó được tạo tự động khi lưu vào DB

    @NotNull(message = "CATEGORY_ID_REQUIRED")
    Long categoryId; // ID của danh mục công việc

    // Các trường tùy chọn (có thể là null)
    Long jobLevelId; // ID của cấp độ công việc
    Long jobTypeId;  // ID của loại công việc
    Long educationId;
    Long experienceId;
}