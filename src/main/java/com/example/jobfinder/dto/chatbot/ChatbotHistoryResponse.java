// src/main/java/com/example/jobfinder/dto/response/ChatbotHistoryResponse.java
package com.example.jobfinder.dto.chatbot;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotHistoryResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String message;
    private String response;
    private LocalDateTime createdAt;
}