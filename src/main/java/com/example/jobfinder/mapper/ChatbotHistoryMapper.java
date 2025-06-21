// src/main/java/com/example/jobfinder/mapper/ChatbotHistoryMapper.java
package com.example.jobfinder.mapper;
import com.example.jobfinder.model.ChatbotHistory;
import com.example.jobfinder.dto.chatbot.ChatbotMessageRequest;
import com.example.jobfinder.dto.chatbot.ChatbotHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ChatbotHistoryMapper {

    // Ánh xạ ChatbotMessageRequest sang ChatbotHistory entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // User được set trong service
    @Mapping(target = "response", ignore = true) // Response được chatbot tạo và set trong service
    @Mapping(target = "createdAt", ignore = true) // Set tự động bởi @PrePersist
    ChatbotHistory toChatbotHistory(ChatbotMessageRequest request);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "userEmail")
    ChatbotHistoryResponse toChatbotHistoryResponse(ChatbotHistory chatbotHistory);

    List<ChatbotHistoryResponse> toChatbotHistoryResponseList(List<ChatbotHistory> chatbotHistories);

    // Không có update cho ChatbotHistory vì thường tin nhắn không sửa đổi.
    // Nếu có, bạn cần thêm một ChatbotUpdateRequest DTO riêng biệt.
}