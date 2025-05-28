package com.example.jobfinder.repository;

import com.example.jobfinder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByVerificationToken(String verificationToken);
    User findByResetPasswordToken(String resetPasswordToken);
}
