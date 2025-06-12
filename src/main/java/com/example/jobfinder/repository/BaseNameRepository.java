// repository/BaseNameRepository.java
package com.example.jobfinder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.Optional;

@NoRepositoryBean
public interface BaseNameRepository<T, ID> extends JpaRepository<T, ID> {
    boolean existsByName(String name);
    Optional<T> findByName(String name);

    // Phương thức mặc định để lấy ID của entity.
    default ID getId(T entity) {
        try {
            // Sử dụng reflection để gọi phương thức getId()
            return (ID) entity.getClass().getMethod("getId").invoke(entity);
        } catch (Exception e) {
            // Xử lý hoặc ném ngoại lệ nếu không tìm thấy phương thức getId()
            throw new RuntimeException("Entity " + entity.getClass().getName() + " does not have a getId() method accessible.", e);
        }
    }
}