// src/main/java/com/example/jobfinder/dto/response/MessageResponse.java
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
public class MessageResponse {
    private Long id; // ID tin nhắn
    private Long conversationId;
    private Long senderId;
    private String senderEmail;
    private String senderFullName; // Tên hiển thị của người gửi
    private Long receiverId;
    private String receiverEmail;
    private String receiverFullName; // Tên hiển thị của người nhận
    private String content;
    private LocalDateTime sentAt;
    private Boolean isRead;
}