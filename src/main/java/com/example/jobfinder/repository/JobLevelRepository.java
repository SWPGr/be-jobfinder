// src/main/java/com/example/jobfinder/repository/JobLevelRepository.java
package com.example.jobfinder.repository;

import com.example.jobfinder.model.JobLevel; // Import entity JobLevel
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Thường dùng cho các phương thức findBy...

@Repository // Đánh dấu đây là một Spring Data Repository
public interface JobLevelRepository extends JpaRepository<JobLevel, Long> {
    // JpaRepository<T, ID>
    // T: Loại Entity mà Repository này quản lý (JobLevel)
    // ID: Loại của khóa chính (Primary Key) của Entity đó (Long, vì id của JobLevel là Long)

    // Bạn có thể thêm các phương thức tùy chỉnh nếu cần, ví dụ:
    Optional<JobLevel> findByName(String name);
}