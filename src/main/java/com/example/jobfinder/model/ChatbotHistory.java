// src/main/java/com/example/jobfinder/model/ChatbotHistory.java
package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonManagedReference; // Import nếu cần thiết cho User
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonManagedReference("user-chatbotHistories") // Thêm nếu bạn có @JsonBackReference trong User
    private User user;

    @Column(columnDefinition = "TEXT") // Sử dụng TEXT cho nội dung dài
    private String message; // Tin nhắn của người dùng

    @Column(columnDefinition = "TEXT")
    private String response; // Phản hồi của chatbot

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}