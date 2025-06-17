package com.example.jobfinder.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor // Constructor không đối số
@AllArgsConstructor // Constructor với tất cả đối số
public class ChatResponse {
    @NonNull
    private String answer; // Câu trả lời từ AI
}