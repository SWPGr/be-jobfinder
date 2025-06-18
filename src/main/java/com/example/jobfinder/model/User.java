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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails { // Implement UserDetails cho Spring Security
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "is_premium", columnDefinition = "BOOLEAN DEFAULT FALSE") // Đảm bảo đúng kiểu dữ liệu
    private Boolean isPremium;

    @Column(name = "reset_password_token", length = 255)
    private String resetPasswordToken;

    @Column(name = "reset_password_expiry")
    private LocalDateTime resetPasswordExpiry;

    @Column(name = "verification_token", length = 255)
    private String verificationToken;

    @Column(name = "verified", columnDefinition = "INT DEFAULT 0") // Đảm bảo đúng kiểu dữ liệu INT
    private Integer verified; // Sửa từ Boolean sang Integer theo DB schema

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Mối quan hệ ---

    // Một User có một Role
    // name = "role_id" là tên cột khóa ngoại trong bảng 'users'
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonManagedReference("role-users")
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getIsPremium() {
        return isPremium;
    }

    public void setIsPremium(Boolean premium) {
        isPremium = premium;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public LocalDateTime getResetPasswordExpiry() {
        return resetPasswordExpiry;
    }

    public void setResetPasswordExpiry(LocalDateTime resetPasswordExpiry) {
        this.resetPasswordExpiry = resetPasswordExpiry;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public Integer getVerified() {
        return verified;
    }

    public void setVerified(Integer verified) {
        this.verified = verified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserDetail getUserDetail() {
        return userDetail;
    }

    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<Application> getApplications() {
        return applications;
    }

    public void setApplications(Set<Application> applications) {
        this.applications = applications;
    }

    public Set<Job> getPostedJobs() {
        return postedJobs;
    }

    public void setPostedJobs(Set<Job> postedJobs) {
        this.postedJobs = postedJobs;
    }

    public Set<JobRecommendation> getJobRecommendations() {
        return jobRecommendations;
    }

    public void setJobRecommendations(Set<JobRecommendation> jobRecommendations) {
        this.jobRecommendations = jobRecommendations;
    }

    public Set<SavedJob> getSavedJobs() {
        return savedJobs;
    }

    public void setSavedJobs(Set<SavedJob> savedJobs) {
        this.savedJobs = savedJobs;
    }

    public Set<JobView> getJobViews() {
        return jobViews;
    }

    public void setJobViews(Set<JobView> jobViews) {
        this.jobViews = jobViews;
    }

    public Set<EmployerReview> getReviewsGiven() {
        return reviewsGiven;
    }

    public void setReviewsGiven(Set<EmployerReview> reviewsGiven) {
        this.reviewsGiven = reviewsGiven;
    }

    public Set<EmployerReview> getReviewsReceived() {
        return reviewsReceived;
    }

    public void setReviewsReceived(Set<EmployerReview> reviewsReceived) {
        this.reviewsReceived = reviewsReceived;
    }

    public Set<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(Set<Notification> notifications) {
        this.notifications = notifications;
    }

    // --- Implement UserDetails cho Spring Security ---
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