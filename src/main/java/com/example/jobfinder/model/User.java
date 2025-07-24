package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "is_premium", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isPremium;

    @Column(name = "reset_password_token", length = 255)
    private String resetPasswordToken;

    @Column(name = "reset_password_expiry")
    private LocalDateTime resetPasswordExpiry;

    @Column(name = "verification_token", length = 255)
    private String verificationToken;

    @Column(name = "verified", columnDefinition = "INT DEFAULT 0")
    private Integer verified;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive;

    // --- Mối quan hệ ---

    // Một User có một Role
    // name = "role_id" là tên cột khóa ngoại trong bảng 'users'
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonManagedReference("role-users")
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference("user-userDetail") // Phía "One" sẽ là BackReference
    private UserDetail userDetail;

    // Một User có nhiều Subscription
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("user-subscriptions")
    private Set<Subscription> subscriptions = new HashSet<>();

    // Một User (Job Seeker) có nhiều Application
    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("jobseeker-applications")
    private Set<Application> applications = new HashSet<>();

    // Một User (Employer) có nhiều Job (đăng tuyển)
    @OneToMany(mappedBy = "employer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("employer-jobs")
    private Set<Job> postedJobs = new HashSet<>();

    // Một User (Job Seeker) có nhiều JobRecommendation
    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("jobseeker-recommendations")
    private Set<JobRecommendation> jobRecommendations = new HashSet<>();

    // Một User (Job Seeker) có nhiều SavedJob
    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("jobseeker-savedjobs")
    private Set<SavedJob> savedJobs = new HashSet<>();

    // Một User (Job Seeker) có nhiều JobView
    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("jobseeker-jobviews")
    private Set<JobView> jobViews = new HashSet<>();

    // Một User (Job Seeker) có thể viết nhiều EmployerReview
    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("reviewer-employerreviews")
    private Set<EmployerReview> reviewsGiven = new HashSet<>();

    // Một User (Employer) có thể nhận nhiều EmployerReview
    @OneToMany(mappedBy = "employer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("reviewed-employerreviews")
    private Set<EmployerReview> reviewsReceived = new HashSet<>();

    // Một User có nhiều Notification
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("user-notifications")
    private Set<Notification> notifications = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("user-chatbotHistories") // <-- THÊM DÒNG NÀY
    private Set<ChatbotHistory> chatbotHistories = new HashSet<>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getName())); // Lấy tên role từ entity Role
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Đây là phương thức isEnabled của UserDetails, KHÔNG phải isVerified tự gọi.
        // Bạn có thể sử dụng trường 'verified' của mình ở đây.
        // Dựa trên DB schema, 'verified' là INT, 0 là FALSE, 1 là TRUE.
        return this.verified != null && this.verified == 1;
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isPremium == null) {
            this.isPremium = false;
        }
        if (this.verified == null) {
            this.verified = 0; // Đặt mặc định là 0 nếu null
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}