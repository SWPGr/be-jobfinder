// src/main/java/com/example/jobfinder/repository/JobTypeRepository.java
package com.example.jobfinder.repository;

import com.example.jobfinder.model.JobType; // Import entity JobType
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Thường dùng cho các phương thức findBy...

@Repository // Đánh dấu đây là một Spring Data Repository
public interface JobTypeRepository extends JpaRepository<JobType, Long> {
    // JpaRepository<T, ID>
    // T: Loại Entity mà Repository này quản lý (JobType)
    // ID: Loại của khóa chính (Primary Key) của Entity đó (Long, vì id của JobType là Long)

    // Bạn có thể thêm các phương thức tùy chỉnh nếu cần, ví dụ:
    Optional<JobType> findByName(String name);
}