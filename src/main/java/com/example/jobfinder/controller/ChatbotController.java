// src/main/java/com/example/jobfinder/controller/ChatbotController.java
package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.chatbot.ChatbotMessageRequest;
import com.example.jobfinder.dto.chatbot.ChatbotHistoryResponse;
import com.example.jobfinder.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChatbotController {

    ChatbotService chatbotService;

    @PostMapping("/message")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ChatbotHistoryResponse> sendMessageToChatbot(@RequestBody @Valid ChatbotMessageRequest request) {
        ChatbotHistoryResponse response = chatbotService.sendMessageToChatbot(request);
        return ApiResponse.<ChatbotHistoryResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Message sent to chatbot and history recorded")
                .result(response)
                .build();
    }

    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ChatbotHistoryResponse>> getMyChatbotHistory() {
        List<ChatbotHistoryResponse> response = chatbotService.getMyChatbotHistory();
        return ApiResponse.<List<ChatbotHistoryResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("My chatbot history fetched successfully")
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ChatbotHistoryResponse> getChatbotHistoryById(@PathVariable Long id) {
        ChatbotHistoryResponse response = chatbotService.getChatbotHistoryById(id);
        return ApiResponse.<ChatbotHistoryResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Chatbot history fetched by ID successfully (Admin access)")
                .result(response)
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Phân quyền chi tiết trong service
    public ApiResponse<Void> deleteChatbotHistory(@PathVariable Long id) {
        chatbotService.deleteChatbotHistory(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Chatbot history deleted successfully")
                .build();
    }

    @DeleteMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> clearMyChatbotHistory() {
        chatbotService.clearMyChatbotHistory();
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("All my chatbot history cleared successfully")
                .build();
    }

    @GetMapping("/all-chatbot-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ChatbotHistoryResponse>> getAllChatbotHistory() {
        List<ChatbotHistoryResponse> response = chatbotService.getAllChatbotHistoryForAdmin();
        return ApiResponse.<List<ChatbotHistoryResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("All chatbot history fetched successfully (Admin access)")
                .result(response)
                .build();
    }
}