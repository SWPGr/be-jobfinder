// src/main/java/com/example/jobfinder/mapper/ConversationMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.response.ConversationResponse;
import com.example.jobfinder.model.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMapper {
    // Có thể thêm các mapping tùy chỉnh nếu cần, ví dụ:
    // @Mapping(source = "participant1.id", target = "participant1Id")
    // @Mapping(source = "participant1.email", target = "participant1Email")
    // ConversationResponse toConversationResponse(Conversation conversation);
}