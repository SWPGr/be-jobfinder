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

    // Tìm search history gần nhất của user
    SearchHistory findFirstByUserOrderByCreatedAtDesc(User user);

    // Tìm search history gần nhất theo user ID
    SearchHistory findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    // Đếm số lượng search history của user
    long countByUser(User user);

    // Tìm search histories cũ nhất của user (để xóa khi vượt quá limit)
    List<SearchHistory> findByUserOrderByCreatedAtAsc(User user);

    List<SearchHistory> findByUserAndSearchTypeOrderByCreatedAtDesc(User user, SearchHistory.SearchType type);

    long countByUserAndSearchType(User user, SearchHistory.SearchType type);

    SearchHistory findFirstByUserAndSearchTypeOrderByCreatedAtDesc(User user, SearchHistory.SearchType type);

    // Xóa tất cả lịch sử tìm kiếm của một người dùng
    void deleteByUser(User user);
}