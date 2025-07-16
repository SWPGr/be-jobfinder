// src/main/java/com/example/jobfinder/controller/SearchHistoryController.java
package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.searchHistory.SearchHistoryRequest;
import com.example.jobfinder.dto.searchHistory.SearchHistoryResponse;
import com.example.jobfinder.service.SearchHistoryService;
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
@RequestMapping("/search-history")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SearchHistoryController {

    SearchHistoryService searchHistoryService;

    /**
     * Ghi lại một lịch sử tìm kiếm mới cho người dùng hiện tại.
     * Endpoint: POST /search-history
     * Yêu cầu: Đã xác thực (AUTHENTICATED).
     * @param request SearchHistoryRequest.
     * @return ApiResponse chứa SearchHistoryResponse.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()") // Chỉ người dùng đã đăng nhập mới có thể ghi lịch sử
    public ApiResponse<SearchHistoryResponse> recordSearch(@RequestBody @Valid SearchHistoryRequest request) {
        SearchHistoryResponse response = searchHistoryService.recordSearch(request);
        return ApiResponse.<SearchHistoryResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Search history recorded successfully")
                .result(response)
                .build();
    }

    /**
     * Lấy tất cả lịch sử tìm kiếm của người dùng hiện tại.
     * Endpoint: GET /search-history/my
     * Yêu cầu: Đã xác thực (AUTHENTICATED).
     * @return ApiResponse chứa danh sách SearchHistoryResponse.
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()") // Chỉ người dùng đã đăng nhập mới có thể xem lịch sử của mình
    public ApiResponse<List<SearchHistoryResponse>> getMySearchHistory() {
        List<SearchHistoryResponse> response = searchHistoryService.getMySearchHistory();
        return ApiResponse.<List<SearchHistoryResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("My search history fetched successfully")
                .result(response)
                .build();
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Phân quyền chi tiết được xử lý trong service
    public ApiResponse<Void> deleteSearchHistory(@PathVariable Long id) {
        searchHistoryService.deleteSearchHistory(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Search history deleted successfully")
                .build();
    }

    /**
     * Xóa tất cả lịch sử tìm kiếm của người dùng hiện tại.
     * Endpoint: DELETE /search-history/my
     * Yêu cầu: Đã xác thực (AUTHENTICATED).
     * @return ApiResponse rỗng.
     */
    @DeleteMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> clearMySearchHistory() {
        searchHistoryService.clearMySearchHistory();
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("All my search history cleared successfully")
                .build();
    }

    /**
     * Lấy tất cả lịch sử tìm kiếm trong hệ thống (dành cho Admin).
     * Endpoint: GET /search-history
     * Yêu cầu: Quyền ADMIN.
     * @return ApiResponse chứa danh sách SearchHistoryResponse.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới có thể xem tất cả lịch sử
    public ApiResponse<List<SearchHistoryResponse>> getAllSearchHistory() {
        List<SearchHistoryResponse> response = searchHistoryService.getAllSearchHistoryForAdmin();
        return ApiResponse.<List<SearchHistoryResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("All search history fetched successfully (Admin access)")
                .result(response)
                .build();
    }
}