// D:\Code-Window\JobFinderProject\be-jobfinder\trunglecode\src\main\java\com\example\jobfinder\repository\EducationRepository.java
package com.example.jobfinder.repository;

import com.example.jobfinder.model.Education;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EducationRepository extends BaseNameRepository<Education, Long> {
    Optional<Education> findByName(String educationName);
}