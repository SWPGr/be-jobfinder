package com.example.jobfinder.repository;

import com.example.jobfinder.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Tìm tất cả thông báo cho một người dùng (sắp xếp theo thời gian tạo mới nhất)
    List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);

    // Tìm tất cả thông báo chưa đọc cho một người dùng
    List<Notification> findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
}