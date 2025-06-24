// src/main/java/com/example/jobfinder/dto/response/ConversationResponse.java
package com.example.jobfinder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {
    private Long id;
    private Long participant1Id;
    private String participant1Email;
    private String participant1FullName;
    private Long participant2Id;
    private String participant2Email;
    private String participant2FullName;
    private LocalDateTime lastMessageAt;
    private String lastMessageContent; // Hiển thị nội dung tin nhắn cuối cùng
    private long unreadMessageCount;   // Số tin nhắn chưa đọc trong cuộc trò chuyện này
}