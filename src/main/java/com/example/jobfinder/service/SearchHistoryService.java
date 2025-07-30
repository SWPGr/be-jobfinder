// src/main/java/com/example/jobfinder/service/SearchHistoryService.java
package com.example.jobfinder.service;

import com.example.jobfinder.dto.searchHistory.SearchHistoryRequest;
import com.example.jobfinder.dto.searchHistory.SearchHistoryResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.SearchHistoryMapper;
import com.example.jobfinder.model.SearchHistory;
import com.example.jobfinder.model.User; // Cần User model
import com.example.jobfinder.repository.SearchHistoryRepository;
import com.example.jobfinder.repository.UserRepository; // Cần UserRepository
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SearchHistoryService {
    SearchHistoryRepository searchHistoryRepository;
    SearchHistoryMapper searchHistoryMapper;
    UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public SearchHistoryResponse recordSearch(SearchHistoryRequest request) {
        User authenticatedUser = getAuthenticatedUser();

        SearchHistory searchHistory = searchHistoryMapper.toSearchHistory(request);
        searchHistory.setUser(authenticatedUser);
        SearchHistory savedSearch = searchHistoryRepository.save(searchHistory);
        log.info("Ghi lại lịch sử tìm kiếm cho user {}: {}", authenticatedUser.getEmail(), request.getSearchQuery());
        return searchHistoryMapper.toSearchHistoryResponse(savedSearch);
    }

    @Transactional(readOnly = true)
    public List<SearchHistoryResponse> getMySearchHistory() {
        User authenticatedUser = getAuthenticatedUser();
        List<SearchHistory> searchHistories = searchHistoryRepository.findByUserOrderByCreatedAtDesc(authenticatedUser);
        log.info("Lấy lịch sử tìm kiếm của user {}. Số lượng: {}", authenticatedUser.getEmail(), searchHistories.size());
        return searchHistoryMapper.toSearchHistoryResponseList(searchHistories);
    }

    @Transactional
    public void deleteSearchHistory(Long id) {
        User authenticatedUser = getAuthenticatedUser();
        SearchHistory searchHistory = searchHistoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!authenticatedUser.getRole().getName().equals("ADMIN") && !searchHistory.getUser().getId().equals(authenticatedUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        searchHistoryRepository.delete(searchHistory);
        log.info("Xóa lịch sử tìm kiếm ID {} bởi user {}", id, authenticatedUser.getEmail());
    }

    @Transactional
    public void clearMySearchHistory() {
        User authenticatedUser = getAuthenticatedUser();
        searchHistoryRepository.deleteByUser(authenticatedUser);
        log.info("Xóa tất cả lịch sử tìm kiếm của user {}", authenticatedUser.getEmail());
    }

    @Transactional(readOnly = true)
    public List<SearchHistoryResponse> getAllSearchHistoryForAdmin() {
        log.info("Admin: Lấy tất cả lịch sử tìm kiếm trong hệ thống.");
        List<SearchHistory> searchHistories = searchHistoryRepository.findAll();
        return searchHistoryMapper.toSearchHistoryResponseList(searchHistories);
    }
}