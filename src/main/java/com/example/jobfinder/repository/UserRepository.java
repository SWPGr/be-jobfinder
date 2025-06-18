package com.example.jobfinder.repository;

import com.example.jobfinder.model.User;
import com.example.jobfinder.util.QueryConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    // Cập nhật phương thức này để thêm tham số `verified`
    @Query(QueryConstants.FIND_USERS_BY_CRITERIA)
    List<User> findUsersByCriteria(@Param("email") String email,
                                   @Param("fullName") String fullName,
                                   @Param("roleName") String roleName,
                                   @Param("location") String location,
                                   @Param("yearsExperience") Integer yearsExperience,
                                   @Param("isPremium") Boolean isPremium,
                                   @Param("verified") Integer verified); // <-- Thêm tham số này
}