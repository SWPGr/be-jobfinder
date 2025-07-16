package com.example.jobfinder.dto.search;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryResponse {
    private Long id;
    private String searchQuery;
    private LocalDateTime createdAt;
}
