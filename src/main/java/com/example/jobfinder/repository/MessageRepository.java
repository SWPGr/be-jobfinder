// src/main/java/com/example/jobfinder/repository/MessageRepository.java
package com.example.jobfinder.repository;

import com.example.jobfinder.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Tìm tất cả tin nhắn trong một cuộc trò chuyện, sắp xếp theo thời gian gửi tăng dần
    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

    // Đếm số lượng tin nhắn chưa đọc của một người nhận cụ thể trong một cuộc trò chuyện
    long countByConversationIdAndReceiverIdAndIsReadFalse(Long conversationId, Long receiverId);
}