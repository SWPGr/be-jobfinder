package com.example.jobfinder.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;


@Entity
@Table(name = "user")
@Getter
@Setter
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, length = 255)
    private String email;
    @Column(length = 255)
    private String password;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private String verificationToken;
    private boolean verified;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;
    @Column(name = "reset_password_expiry")
    private LocalDateTime resetPasswordExpiry;
}
