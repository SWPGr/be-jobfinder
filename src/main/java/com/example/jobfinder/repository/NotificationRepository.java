package com.example.jobfinder.repository;

import com.example.jobfinder.model.Notification;
import com.example.jobfinder.util.QueryConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Tìm tất cả thông báo cho một người dùng (sắp xếp theo thời gian tạo mới nhất)
    List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);

    // Tìm tất cả thông báo chưa đọc cho một người dùng
    List<Notification> findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(Long userId);


    @Query(QueryConstants.FIND_NOTIFICATIONS_BY_CRITERIA)
    List<Notification> findNotificationsByCriteria(@Param("userEmail") String userEmail,
                                                   @Param("isRead") Boolean isRead,
                                                   @Param("messageKeyword") String messageKeyword);
}