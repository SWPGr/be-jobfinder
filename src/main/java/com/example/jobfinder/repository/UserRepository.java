package com.example.jobfinder.repository;

import com.example.jobfinder.model.User;
import com.example.jobfinder.util.QueryConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    Optional<User>  findByResetPasswordToken(String resetPasswordToken);

    Optional<User> findFirstByOrderByCreatedAtAsc();

    default Optional<User> findByEmailIgnoringActiveFilter(String email) {
        throw new UnsupportedOperationException("This method should not be called directly. Use CustomUserDetailsService.");
    }


    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.name = :roleName AND u.createdAt <= :endDate")
    long countUsersByRoleNameAndCreatedAtBeforeOrEquals(@Param("roleName") String roleName, @Param("endDate") LocalDateTime endDate);

    // Đếm tổng số người dùng được tạo cho đến một ngày cụ thể
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt <= :endDate")
    long countTotalUsersCreatedBeforeOrEquals(@Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();

    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE r.name = :roleName")
    long countUsersByRoleName(@Param("roleName") String roleName);

    @Query("SELECT u, ud, r " +
            "FROM User u " +
            "LEFT JOIN u.userDetail ud " +
            "LEFT JOIN u.role r " +
            "WHERE (:roleName IS NULL OR r.name = :roleName)")
    List<Object[]> findUsersWithDetailsAndRole(@Param("roleName") String roleName);

    @Query(QueryConstants.FIND_USERS_BY_CRITERIA)
    List<User> findUsersByCriteria(@Param("email") String email,
                                   @Param("fullName") String fullName,
                                   @Param("roleName") String roleName,
                                   @Param("location") String location,
                                   @Param("isPremium") Boolean isPremium,
                                   @Param("verified") Integer verified,
                                   @Param("resumeUrl") String resumeUrl,
                                   @Param("companyName") String companyName,
                                   @Param("website") String website);


    //Thêm trường active để kiểm tra:
    Optional<User> findByEmailAndIsActive(String email, boolean isActive);

    Boolean existsByEmailAndIsActive(String email, boolean isActive);

    List<User> findByCreatedAtBetweenAndIsActive(LocalDateTime start, LocalDateTime end, boolean isActive);


}