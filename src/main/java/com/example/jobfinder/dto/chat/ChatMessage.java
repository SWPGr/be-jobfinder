// src/main/java/com/example/jobfinder/dto/chat/ChatMessage.java
package com.example.jobfinder.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    private Long conversationId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private LocalDateTime sentAt;
    private Boolean isRead;
}