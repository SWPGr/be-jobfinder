// src/main/java/com/example/jobfinder/repository/JobLevelRepository.java
package com.example.jobfinder.repository;

import com.example.jobfinder.model.JobLevel; // Import entity JobLevel
import org.springframework.stereotype.Repository;
@Repository // Đánh dấu đây là một Spring Data Repository
public interface JobLevelRepository extends BaseNameRepository<JobLevel, Long> {

}