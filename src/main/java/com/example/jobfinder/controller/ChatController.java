// src/main/java/com/example/jobfinder/controller/ChatController.java
package com.example.jobfinder.controller;

import com.example.jobfinder.dto.chat.ChatMessage;
import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.chat.ConversationResponse;
import com.example.jobfinder.dto.chat.MessageResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.UserRepository;
import com.example.jobfinder.service.MessageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // Import này để lấy UserDetails

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/chat") // Endpoint REST cho các API liên quan đến chat (không phải WebSocket)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChatController {

    SimpMessagingTemplate messagingTemplate; // Dùng để gửi tin nhắn qua WebSocket
    MessageService messageService;
    UserRepository userRepository; // Để lấy thông tin user

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(
            @Payload ChatMessage chatMessage
    ) {
        return chatMessage;
    }

    @MessageMapping("/chat.markAsRead")
    public void markMessagesAsRead(@Payload Long conversationId, @org.jetbrains.annotations.NotNull SimpMessageHeaderAccessor headerAccessor) {
        Authentication authentication = (Authentication) headerAccessor.getUser();
        if (authentication == null || authentication.getName() == null) {
            log.warn("Unauthorized WebSocket markAsRead request: No authentication principal.");
            return;
        }

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        log.info("Received request to mark conversation {} as read by user {}", conversationId, currentUser.getId());
        try {
            messageService.markMessagesAsRead(conversationId, currentUser.getId());
            // Có thể gửi thông báo cập nhật cho cả hai bên nếu cần
        } catch (AppException e) {
            log.error("Error marking messages as read: {}", e.getMessage());
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(currentUser.getId()),
                    "/queue/errors",
                    "Error marking messages as read: " + e.getMessage()
            );
        } catch (Exception e) {
            log.error("Unexpected error during markMessagesAsRead: {}", e.getMessage(), e);
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(currentUser.getId()),
                    "/queue/errors",
                    "Unexpected error: " + e.getMessage()
            );
        }
    }

    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()") // Yêu cầu xác thực
    public ApiResponse<List<ConversationResponse>> getUserConversations() {
        log.info("Received REST request to get current user's conversations.");
        List<ConversationResponse> conversations = messageService.getUserConversations();
        return ApiResponse.<List<ConversationResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("User conversations fetched successfully")
                .result(conversations)
                .build();
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()") // Yêu cầu xác thực
    public ApiResponse<List<MessageResponse>> getConversationMessages(@PathVariable Long conversationId) {
        log.info("Received REST request to get messages for conversation ID: {}", conversationId);
        List<MessageResponse> messages = messageService.getConversationMessages(conversationId);
        return ApiResponse.<List<MessageResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Messages for conversation fetched successfully")
                .result(messages)
                .build();
    }

}