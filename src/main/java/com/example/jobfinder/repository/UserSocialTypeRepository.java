package com.example.jobfinder.repository;

import com.example.jobfinder.model.UserSocialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSocialTypeRepository extends JpaRepository<UserSocialType, Long> {
    List<UserSocialType> findByUserDetail_Id(Long userDetailId);
    Optional<UserSocialType> findByUserDetail_IdAndSocialType_Id(Long userDetailId, Long socialTypeId);
    boolean existsByUserDetail_IdAndSocialType_Id(Long userDetailId, Long socialTypeId);
}