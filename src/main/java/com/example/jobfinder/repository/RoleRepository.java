package com.example.jobfinder.repository;

import com.example.jobfinder.model.Role;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends BaseNameRepository<Role, Long> {
    Optional<Role> findByName(String roleName);
}
