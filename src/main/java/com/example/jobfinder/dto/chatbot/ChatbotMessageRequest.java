// src/main/java/com/example/jobfinder/dto/request/ChatbotMessageRequest.java
package com.example.jobfinder.dto.chatbot;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotMessageRequest {
    @NotBlank(message = "Message không được để trống")
    private String message; // Tin nhắn của người dùng
}