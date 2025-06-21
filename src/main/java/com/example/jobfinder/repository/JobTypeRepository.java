// src/main/java/com/example/jobfinder/repository/JobTypeRepository.java
package com.example.jobfinder.repository;

import com.example.jobfinder.model.JobType; // Import entity JobType
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Thường dùng cho các phương thức findBy...

@Repository // Đánh dấu đây là một Spring Data Repository
public interface JobTypeRepository extends BaseNameRepository<JobType, Long> {
    Optional<JobType> findByName(String jobTypeName);
}