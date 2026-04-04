package com.example.jobfinder.repository;

import com.example.jobfinder.dto.job.TopCategoryProjection;
import com.example.jobfinder.model.Category;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // Đánh dấu interface này là một Spring Data Repository
public interface CategoryRepository extends BaseNameRepository<Category, Long> {
    Optional<Category> findByName(String categoryName);

    @Query(value = """
    SELECT c.id AS categoryId, c.category_name AS categoryName, COUNT(j.id) AS jobCount
        FROM categories c
        JOIN jobs j ON j.category_id = c.id
        WHERE j.active = TRUE
        GROUP BY c.id, c.category_name\s
        ORDER BY jobCount DESC
        LIMIT 10
""", nativeQuery = true)
    List<TopCategoryProjection> findTopCategoriesWithMostJobs();

}
