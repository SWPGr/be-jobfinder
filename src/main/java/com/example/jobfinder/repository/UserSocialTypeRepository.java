package com.example.jobfinder.repository;

import com.example.jobfinder.model.UserSocialType;
import com.example.jobfinder.util.QueryConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSocialTypeRepository extends JpaRepository<UserSocialType, Long> {
    List<UserSocialType> findByUserDetail_Id(Long userDetailId);
    Optional<UserSocialType> findByUserDetail_IdAndSocialType_Id(Long userDetailId, Long socialTypeId);
    boolean existsByUserDetail_IdAndSocialType_Id(Long userDetailId, Long socialTypeId);

    @Query(QueryConstants.FIND_USER_SOCIAL_TYPES_BY_CRITERIA)
    List<UserSocialType> findUserSocialTypesByCriteria(@Param("userFullName") String userFullName,
                                                       @Param("socialTypeName") String socialTypeName,
                                                       @Param("url") String url);
}