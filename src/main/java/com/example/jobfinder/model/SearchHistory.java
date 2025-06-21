// src/main/java/com/example/jobfinder/model/SearchHistory.java
package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonManagedReference; // Import nếu cần thiết cho User
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Mối quan hệ nhiều lịch sử tìm kiếm cho một User
    @JoinColumn(name = "user_id", nullable = false) // Tên cột khóa ngoại trong bảng search_history
    @JsonManagedReference("user-searchHistories") // Thêm nếu bạn có @JsonBackReference trong User
    private User user;

    @Column(name = "search_query", length = 255)
    private String searchQuery;

    @Column(name = "created_at") // Đảm bảo khớp với TIMESTAMP trong DB, thường tự động tạo
    private LocalDateTime createdAt;

    // Lifecycle callbacks để tự động quản lý created_at
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) { // Chỉ set nếu nó chưa được set (có thể từ client)
            this.createdAt = LocalDateTime.now();
        }
    }
}