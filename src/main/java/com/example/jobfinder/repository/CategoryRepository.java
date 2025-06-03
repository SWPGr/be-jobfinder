package com.example.jobfinder.repository;

import com.example.jobfinder.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Đánh dấu interface này là một Spring Data Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
