// src/main/java/com/example/jobfinder/mapper/SearchHistoryMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.model.SearchHistory;
import com.example.jobfinder.dto.searchHistory.SearchHistoryRequest;
import com.example.jobfinder.dto.searchHistory.SearchHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SearchHistoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // User được set trong service
    @Mapping(target = "createdAt", ignore = true) // Set tự động bởi @PrePersist
    SearchHistory toSearchHistory(SearchHistoryRequest request);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "userEmail")
    SearchHistoryResponse toSearchHistoryResponse(SearchHistory searchHistory);

    List<SearchHistoryResponse> toSearchHistoryResponseList(List<SearchHistory> searchHistories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateSearchHistory(@MappingTarget SearchHistory searchHistory, SearchHistoryRequest request);
}