package com.example.jobfinder.service;

import com.example.jobfinder.dto.notification.NotificationRequest;
import com.example.jobfinder.dto.notification.NotificationResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.NotificationMapper;
import com.example.jobfinder.model.Notification;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.NotificationRepository;
import com.example.jobfinder.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {

    NotificationRepository notificationRepository;
    UserRepository userRepository;
    NotificationMapper notificationMapper;

    public NotificationResponse createNotification(Long userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .build();

        return notificationMapper.toNotificationResponse(notificationRepository.save(notification));
    }

    public List<NotificationResponse> getNotificationsForUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Notification> notifications = notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        return notificationMapper.toNotificationResponseList(notifications);
    }

    public List<NotificationResponse> getUnreadNotificationsForUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Notification> notifications = notificationRepository.findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return notificationMapper.toNotificationResponseList(notifications);
    }

    @Transactional
    public NotificationResponse markNotificationAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // Đảm bảo người dùng hiện tại là người sở hữu thông báo
        if (!notification.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        notification.setIsRead(true);
        return notificationMapper.toNotificationResponse(notificationRepository.save(notification));
    }


    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        notificationRepository.delete(notification);
    }
}