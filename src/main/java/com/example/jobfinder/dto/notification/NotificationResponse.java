package com.example.jobfinder.dto.notification;

import com.example.jobfinder.dto.user.UserResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    Long id;
    UserResponse user;
    String message;
    Boolean isRead;
    LocalDateTime createdAt;
}