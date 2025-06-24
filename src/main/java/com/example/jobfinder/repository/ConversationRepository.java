// src/main/java/com/example/jobfinder/repository/ConversationRepository.java
package com.example.jobfinder.repository;

import com.example.jobfinder.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Tìm kiếm cuộc trò chuyện giữa hai người tham gia, không phân biệt thứ tự ID
    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.participant1.id = :userId1 AND c.participant2.id = :userId2) OR " +
            "(c.participant1.id = :userId2 AND c.participant2.id = :userId1)")
    Optional<Conversation> findByParticipants(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // Tìm kiếm tất cả cuộc trò chuyện mà một người dùng là một trong các bên tham gia
    List<Conversation> findByParticipant1_IdOrParticipant2_IdOrderByLastMessageAtDesc(Long userId1, Long userId2);
}