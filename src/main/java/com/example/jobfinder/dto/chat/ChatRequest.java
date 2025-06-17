package com.example.jobfinder.dto.chat;

import lombok.Data;
import lombok.NonNull;

@Data // Tự động tạo getters, setters, toString, equals, hashCode
public class ChatRequest {
    @NonNull // Yêu cầu trường này không được null
    private String message; // Tin nhắn từ người dùng
}