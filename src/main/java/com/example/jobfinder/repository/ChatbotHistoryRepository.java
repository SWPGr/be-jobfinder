// src/main/java/com/example/jobfinder/repository/ChatbotHistoryRepository.java
package com.example.jobfinder.repository;

import com.example.jobfinder.model.ChatbotHistory;
import com.example.jobfinder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatbotHistoryRepository extends JpaRepository<ChatbotHistory, Long> {
    // Tìm lịch sử chatbot của một người dùng cụ thể, sắp xếp theo thời gian mới nhất
    List<ChatbotHistory> findByUserOrderByCreatedAtDesc(User user);

    // Tìm lịch sử chatbot của một người dùng theo ID của User
    List<ChatbotHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ChatbotHistory> findAllByOrderByCreatedAtDesc(); // Thêm phương thức này cho ADMIN

    @Modifying // Bắt buộc cho các query thay đổi dữ liệu (INSERT, UPDATE, DELETE)
    @Query("DELETE FROM ChatbotHistory h WHERE h.user.id = :userId")
    void deleteByUserId(Long userId);

    // Xóa tất cả lịch sử chatbot của một người dùng
    void deleteByUser(User user);
}