// src/main/java/com/example/jobfinder/service/MessageService.java
package com.example.jobfinder.service;

import com.example.jobfinder.dto.chat.ChatMessage;
import com.example.jobfinder.dto.chat.ConversationResponse;
import com.example.jobfinder.dto.chat.MessageResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.MessageMapper;
import com.example.jobfinder.model.Conversation;
import com.example.jobfinder.model.Message;
import com.example.jobfinder.model.User;
import com.example.jobfinder.model.UserDetail;
import com.example.jobfinder.repository.ConversationRepository;
import com.example.jobfinder.repository.MessageRepository;
import com.example.jobfinder.repository.UserDetailsRepository;
import com.example.jobfinder.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MessageService {

    UserRepository userRepository;
    UserDetailsRepository userDetailsRepository;
    ConversationRepository conversationRepository;
    MessageRepository messageRepository;
    MessageMapper messageMapper;

    @Transactional
    public MessageResponse saveAndDeliverMessage(ChatMessage chatMessage) {
        log.info("Attempting to save and deliver message from {} to {}: {}",
                chatMessage.getSenderId(), chatMessage.getReceiverId(), chatMessage.getContent());

        User sender = userRepository.findById(chatMessage.getSenderId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User receiver = userRepository.findById(chatMessage.getReceiverId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Đảm bảo participant1_id luôn nhỏ hơn participant2_id để duy trì UNIQUE constraint
        Long p1Id = Math.min(sender.getId(), receiver.getId());
        Long p2Id = Math.max(sender.getId(), receiver.getId());

        log.info("p1Id: {}, p2Id: {}", p1Id, p2Id);

        // Tìm hoặc tạo Conversation
        Conversation conversation = conversationRepository.findByParticipants(p1Id, p2Id)
                .orElseGet(() -> {
                    log.info("Creating new conversation between {} and {}", p1Id, p2Id);
                    return conversationRepository.save(Conversation.builder()
                            .participant1(userRepository.findById(p1Id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)))
                            .participant2(userRepository.findById(p2Id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)))
                            .createdAt(LocalDateTime.now())
                            .build());
                });

        // Tạo Message
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .receiver(receiver)
                .content(chatMessage.getContent())
                .sentAt(LocalDateTime.now()) // Thời gian gửi là thời gian server nhận được
                .isRead(false) // Mặc định là chưa đọc
                .build();

        System.out.println(message);

        Message savedMessage = messageRepository.save(message);

        conversation.setLastMessageAt(savedMessage.getSentAt());
        conversationRepository.save(conversation); // Lưu cập nhật

        log.info("Message saved with ID: {}", savedMessage.getId());
        return messageMapper.toMessageResponse(savedMessage);
    }

    public List<MessageResponse> getConversationMessages(Long conversationId) {
        log.info("Fetching messages for conversation ID: {}", conversationId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND)); // Cần ErrorCode này

        // Đảm bảo người dùng hiện tại là một trong các participant của cuộc trò chuyện
        if (!conversation.getParticipant1().getId().equals(currentUser.getId()) &&
                !conversation.getParticipant2().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED); // Cần ErrorCode này
        }

        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);

        // Đánh dấu tin nhắn là đã đọc cho người dùng hiện tại nếu họ là người nhận và tin nhắn chưa đọc
        messages.stream()
                .filter(msg -> msg.getReceiver().getId().equals(currentUser.getId()) && !msg.getIsRead())
                .forEach(msg -> {
                    msg.setIsRead(true);
                    messageRepository.save(msg); // Lưu trạng thái đã đọc
                });

        return messages.stream()
                .map(messageMapper::toMessageResponse)
                .collect(Collectors.toList());
    }

    public List<ConversationResponse> getUserConversations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        log.info("Fetching conversations for user: {}", currentUserEmail);

        List<Conversation> conversations = conversationRepository.findByParticipant1_IdOrParticipant2_IdOrderByLastMessageAtDesc(
                currentUser.getId(), currentUser.getId());

        return conversations.stream()
                .map(conversation -> {
                    User otherParticipant = conversation.getOtherParticipant(currentUser);
                    if (otherParticipant == null) {
                        log.warn("Invalid conversation state: Current user {} not found in participants of conversation {}", currentUser.getId(), conversation.getId());
                        return null; // Hoặc ném lỗi nếu trạng thái dữ liệu không hợp lệ
                    }

                    // Lấy tin nhắn cuối cùng để hiển thị nội dung tóm tắt
                    String lastMessageContent = messageRepository.findByConversationIdOrderBySentAtAsc(conversation.getId())
                            .stream()
                            .reduce((first, second) -> second) // Lấy phần tử cuối cùng
                            .map(Message::getContent)
                            .orElse("Chưa có tin nhắn nào.");

                    // Đếm tin nhắn chưa đọc
                    long unreadCount = messageRepository.countByConversationIdAndReceiverIdAndIsReadFalse(conversation.getId(), currentUser.getId());

                    // Lấy thông tin chi tiết của người tham gia khác để hiển thị
                    UserDetail otherUserDetail = userDetailsRepository.findByUserId(otherParticipant.getId()).orElse(null);
                    String otherParticipantFullName = (otherUserDetail != null && otherUserDetail.getFullName() != null) ? otherUserDetail.getFullName() : otherParticipant.getEmail();

                    return ConversationResponse.builder()
                            .id(conversation.getId())
                            .participant1Id(conversation.getParticipant1().getId())
                            .participant1Email(conversation.getParticipant1().getEmail())
                            .participant1FullName(userDetailsRepository.findByUserId(conversation.getParticipant1().getId()).map(UserDetail::getFullName).orElse(conversation.getParticipant1().getEmail()))
                            .participant2Id(conversation.getParticipant2().getId())
                            .participant2Email(conversation.getParticipant2().getEmail())
                            .participant2FullName(userDetailsRepository.findByUserId(conversation.getParticipant2().getId()).map(UserDetail::getFullName).orElse(conversation.getParticipant2().getEmail()))
                            .lastMessageAt(conversation.getLastMessageAt())
                            .lastMessageContent(lastMessageContent)
                            .unreadMessageCount(unreadCount)
                            .build();
                })
                .filter(java.util.Objects::nonNull) // Loại bỏ các cuộc trò chuyện bị lỗi (nếu có null)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(Long conversationId, Long userId) {
        log.info("Marking messages as read for conversation {} by user {}", conversationId, userId);
        List<Message> unreadMessages = messageRepository.findByConversationIdOrderBySentAtAsc(conversationId).stream()
                .filter(msg -> msg.getReceiver().getId().equals(userId) && !msg.getIsRead())
                .collect(Collectors.toList());

        for (Message msg : unreadMessages) {
            msg.setIsRead(true);
            messageRepository.save(msg);
        }
        log.info("Marked {} messages as read for conversation {} by user {}", unreadMessages.size(), conversationId, userId);
    }
}