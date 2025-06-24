// src/main/java/com/example/jobfinder/mapper/MessageMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.chat.MessageResponse;
import com.example.jobfinder.model.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring") // Spring component để Spring có thể inject
public interface MessageMapper {

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.email", target = "senderEmail")
    @Mapping(source = "sender.userDetail.fullName", target = "senderFullName") // Giả định User có UserDetail
    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(source = "receiver.email", target = "receiverEmail")
    @Mapping(source = "receiver.userDetail.fullName", target = "receiverFullName") // Giả định User có UserDetail
    @Mapping(source = "conversation.id", target = "conversationId")
    MessageResponse toMessageResponse(Message message);
}