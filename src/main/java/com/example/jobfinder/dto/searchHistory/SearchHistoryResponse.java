// src/main/java/com/example/jobfinder/dto/response/SearchHistoryResponse.java
package com.example.jobfinder.dto.searchHistory;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistoryResponse {
    private Long id;
    private Long userId; // ID của người dùng đã tìm kiếm
    private String userEmail; // Email của người dùng
    private String searchQuery;
    private String searchType;
    private LocalDateTime createdAt;
}