package com.example.jobfinder.repository;

import com.example.jobfinder.model.User;
import com.example.jobfinder.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {
    UserDetails findByUserId(Long userId);
    Optional<UserDetails> findByUser(User user);
}
