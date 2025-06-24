// src/main/java/com/example/jobfinder/controller/ChatController.java
package com.example.jobfinder.controller;

import com.example.jobfinder.dto.chat.ChatMessage;
import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.response.ConversationResponse;
import com.example.jobfinder.dto.response.MessageResponse;
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

    /**
     * Endpoint WebSocket để gửi tin nhắn.
     * Tin nhắn gửi đến "/app/chat.sendMessage" sẽ được xử lý tại đây.
     *
     * @param chatMessage Tin nhắn từ client.
     * @param headerAccessor Dùng để truy cập thông tin header của WebSocket (ví dụ: user principal).
     */
    @MessageMapping("/chat.sendMessage") // Client gửi đến /app/chat.sendMessage
    public void sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Lấy thông tin người gửi từ context bảo mật (sau khi xác thực WebSocket)
        Authentication authentication = (Authentication) headerAccessor.getUser();
        if (authentication == null || authentication.getName() == null) {
            log.warn("Unauthorized WebSocket message received: No authentication principal.");
            // Không ném exception trực tiếp ở đây, vì nó sẽ đóng kết nối.
            // Có thể gửi lỗi ngược lại cho người gửi hoặc log.
            return;
        }

        User sender = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)); // Sender must exist

        // Gán senderId từ người dùng đã xác thực
        chatMessage.setSenderId(sender.getId());

        if (chatMessage.getReceiverId() == null || chatMessage.getSenderId().equals(chatMessage.getReceiverId())) {
            log.warn("Invalid chat message: Receiver ID is missing or is the sender's ID.");
            return;
        }

        log.info("Received message from User {} to User {}: {}",
                chatMessage.getSenderId(), chatMessage.getReceiverId(), chatMessage.getContent());

        try {
            MessageResponse savedMessage = messageService.saveAndDeliverMessage(chatMessage);

            // Gửi tin nhắn đến người gửi (để cập nhật giao diện của họ)
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(savedMessage.getSenderId()), // Convert Long ID to String for user target
                    "/queue/messages", // Destination for private messages
                    savedMessage
            );
            log.info("Sent message back to sender (user {}): {}", savedMessage.getSenderId(), savedMessage.getContent());

            // Gửi tin nhắn đến người nhận
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(savedMessage.getReceiverId()), // Convert Long ID to String for user target
                    "/queue/messages", // Destination for private messages
                    savedMessage
            );
            log.info("Sent message to receiver (user {}): {}", savedMessage.getReceiverId(), savedMessage.getContent());

        } catch (AppException e) {
            log.error("Error processing chat message: {}", e.getMessage());
            // Có thể gửi thông báo lỗi lại cho người gửi
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(chatMessage.getSenderId()),
                    "/queue/errors", // Kênh lỗi
                    "Error: " + e.getMessage()
            );
        } catch (Exception e) {
            log.error("Unexpected error during sendMessage: {}", e.getMessage(), e);
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(chatMessage.getSenderId()),
                    "/queue/errors",
                    "Unexpected error: " + e.getMessage()
            );
        }
    }

    /**
     * Endpoint WebSocket để đánh dấu tin nhắn là đã đọc.
     * Client gửi đến "/app/chat.markAsRead".
     *
     * @param conversationId ID cuộc trò chuyện.
     * @param headerAccessor Dùng để lấy thông tin người dùng.
     */
    @MessageMapping("/chat.markAsRead")
    public void markMessagesAsRead(@Payload Long conversationId, SimpMessageHeaderAccessor headerAccessor) {
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


    // --- API REST cho các tác vụ không trực tiếp qua WebSocket ---

    /**
     * Lấy danh sách tất cả các cuộc trò chuyện của người dùng hiện tại.
     * Sắp xếp theo tin nhắn gần nhất.
     */
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

    /**
     * Lấy tất cả tin nhắn trong một cuộc trò chuyện cụ thể.
     */
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