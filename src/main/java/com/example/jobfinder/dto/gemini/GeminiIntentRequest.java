// src/main/java/com/example/jobfinder/dto/gemini/GeminiIntentRequest.java
package com.example.jobfinder.dto.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data // Tự động tạo getters, setters, toString, equals, hashCode
@NoArgsConstructor // Tự động tạo constructor không đối số
@Builder // Tự động tạo builder pattern cho việc khởi tạo dễ dàng
public class GeminiIntentRequest {
    private List<Content> contents;

    // Lớp nội bộ để biểu diễn cấu trúc 'contents'
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Content {
        private List<Part> parts;
        private String role; // "user" hoặc "model"
    }

    // Lớp nội bộ để biểu diễn cấu trúc 'parts'
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Part {
        private String text; // Phần văn bản của nội dung
    }

    // Constructor tiện lợi cho trường hợp đơn giản chỉ có text trong content, với role "user" mặc định
    public GeminiIntentRequest(List<Content> contents) {
        this.contents = contents;
    }
}