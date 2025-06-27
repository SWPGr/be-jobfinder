package com.example.jobfinder.repository;

import com.example.jobfinder.model.Experience;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperienceRepository extends BaseNameRepository<Experience, Long> {
}
