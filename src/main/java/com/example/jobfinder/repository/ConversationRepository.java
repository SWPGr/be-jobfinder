// src/main/java/com/example/jobfinder/repository/ConversationRepository.java
package com.example.jobfinder.repository;

import com.example.jobfinder.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.participant1.id = :userId1 AND c.participant2.id = :userId2) OR " +
            "(c.participant1.id = :userId2 AND c.participant2.id = :userId1)")
    Optional<Conversation> findByParticipants(Long userId1, Long userId2);

    List<Conversation> findByParticipant1_IdOrParticipant2_IdOrderByLastMessageAtDesc(Long userId1, Long userId2);
}