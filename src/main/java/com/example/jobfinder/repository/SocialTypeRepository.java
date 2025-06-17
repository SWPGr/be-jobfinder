package com.example.jobfinder.repository;

import com.example.jobfinder.model.JobType;
import com.example.jobfinder.model.SocialType;
import com.example.jobfinder.util.QueryConstants;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialTypeRepository extends BaseNameRepository<SocialType, Long> {

    Optional<SocialType> findByName(String socialTypeName); // Derived query

    @Query(QueryConstants.FIND_SOCIAL_TYPES_BY_NAME)
    List<SocialType> findSocialTypesByName(@Param("socialTypeName") String socialTypeName);
}