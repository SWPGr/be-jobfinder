package com.example.jobfinder.repository;

import com.example.jobfinder.model.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetail, Long> {
    UserDetail findByUserId(Long userId);
}
