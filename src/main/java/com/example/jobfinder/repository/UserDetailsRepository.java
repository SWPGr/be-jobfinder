package com.example.jobfinder.repository;

import com.example.jobfinder.model.User;
import com.example.jobfinder.model.UserDetail;
import com.example.jobfinder.util.QueryConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetail, Long> {
    Optional<UserDetail> findByUserId(Long userId);

    Optional<UserDetail> findByUser(User user);
    List<UserDetail> findByUserRoleName(String roleName);
    List<UserDetail> findByCompanyNameContainingIgnoreCase(String companyName);
    List<UserDetail> findByCompanyNameContainingIgnoreCaseAndLocationContainingIgnoreCase(String companyName, String location);

    @Query(QueryConstants.FIND_USER_DETAILS_BY_CRITERIA)
    List<UserDetail> findUserDetailsByCriteria(@Param("fullName") String fullName,
                                               @Param("location") String location,
                                               @Param("yearsExperience") Integer yearsExperience,
                                               @Param("educationType") String educationType,
                                               @Param("companyName") String companyName);
}
