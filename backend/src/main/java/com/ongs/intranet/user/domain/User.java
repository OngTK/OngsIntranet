package com.ongs.intranet.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String googleId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(name = "department_id")
    private Long departmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String position;
    private String phone;
    private String profileImageUrl;
    private String googleCalendarRefreshToken;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static User create(String googleId, String email, String name, String profileImageUrl) {
        User user = new User();
        user.googleId = googleId;
        user.email = email;
        user.name = name;
        user.profileImageUrl = profileImageUrl;
        user.role = Role.EMPLOYEE;
        user.isActive = true;
        return user;
    }

    public void updateProfile(String phone, String profileImageUrl) {
        if (phone != null) this.phone = phone;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void changeRole(Role role) {
        this.role = role;
    }
}
