// src/main/java/com/example/jobfinder/repository/SearchHistoryRepository.java
package com.example.jobfinder.repository;

import com.example.jobfinder.model.SearchHistory;
import com.example.jobfinder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    // Tìm lịch sử tìm kiếm của một người dùng cụ thể, sắp xếp theo thời gian mới nhất
    List<SearchHistory> findByUserOrderByCreatedAtDesc(User user);

    // Tìm lịch sử tìm kiếm của một người dùng theo ID của User
    List<SearchHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Xóa tất cả lịch sử tìm kiếm của một người dùng
    void deleteByUser(User user);
}