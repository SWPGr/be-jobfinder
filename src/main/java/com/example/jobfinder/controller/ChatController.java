package com.example.jobfinder.controller;

import com.example.jobfinder.dto.chat.ChatRequest;
import com.example.jobfinder.dto.chat.ChatResponse;
import com.example.jobfinder.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException; // Import cho xử lý validation
import org.springframework.web.bind.annotation.ExceptionHandler; // Import cho xử lý ngoại lệ
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus; // Import cho @ResponseStatus
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid; // Import @Valid cho validation

import java.io.IOException; // Import IOException cho xử lý lỗi

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor // Tự động tạo constructor với các final fields
@Slf4j
public class ChatController {

    private final ChatbotService chatbotService;

    @PostMapping
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat message from frontend: {}", request.getMessage());

        try {
            // chatbotService.getChatResponse đã trả về ChatResponse rồi
            ChatResponse chatbotResponse = chatbotService.getChatResponse(request.getMessage());
            // Trả về 200 OK với đối tượng ChatResponse
            return ResponseEntity.ok(chatbotResponse);
        } catch (Exception e) {
            log.error("An unexpected error occurred during chat processing: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponse("An unexpected error occurred. Please try again later."));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ChatResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Lấy lỗi đầu tiên từ kết quả validation
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("Validation error for chat request: {}", errorMessage);
        return ResponseEntity.badRequest().body(new ChatResponse(errorMessage));
    }
}