// src/main/java/com/example/jobfinder/dto/request/SearchHistoryRequest.java
package com.example.jobfinder.dto.searchHistory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistoryRequest {
    // user_id sẽ được lấy từ JWT token của người dùng hiện tại, không cần gửi lên
    @NotBlank(message = "Search query không được để trống")
    @Size(max = 255, message = "Search query không được vượt quá 255 ký tự")
    private String searchQuery;
}