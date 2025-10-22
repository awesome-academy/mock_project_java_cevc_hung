package com.mock_project_java_cevc_hung.hunglpmockjava.entity;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 11)
    private int phone_number;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String address;

    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Provider provider = Provider.LOCAL;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingEntity> bookings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewEntity> reviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewLikeEntity> reviewLikes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    private List<BankAccountEntity> bankAccounts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityLogEntity> activityLogs;

    public enum Role { USER, ADMIN }
    public enum Provider { LOCAL, GOOGLE }
}
