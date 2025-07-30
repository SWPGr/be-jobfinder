package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.notification.NotificationRequest;
import com.example.jobfinder.dto.notification.NotificationResponse;
import com.example.jobfinder.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class}) // <-- Cần UserMapper để map User sang UserResponse
public interface NotificationMapper {
    NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    // Khi tạo Notification từ request, User và các trường khác sẽ được set trong service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // User sẽ được set thủ công trong service
    @Mapping(target = "isRead", ignore = true) // Mặc định là false trong entity
    @Mapping(target = "createdAt", ignore = true) // Tự động tạo bởi @CreationTimestamp
    Notification toNotification(NotificationRequest request);

    // Khi chuyển Notification Entity sang Response DTO
    @Mapping(source = "user", target = "user")
    @Mapping(source = "job.id", target = "jobId")// Map User entity to UserResponse DTO
    NotificationResponse toNotificationResponse(Notification notification);

    List<NotificationResponse> toNotificationResponseList(List<Notification> notifications);
}