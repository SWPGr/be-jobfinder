package com.example.jobfinder.repository;

import com.example.jobfinder.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.jobfinder.model.Role;

@Repository
public interface RoleRepository extends BaseNameRepository<Role, Long> { }
