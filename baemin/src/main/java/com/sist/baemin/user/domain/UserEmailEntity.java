package com.sist.baemin.user.domain;

import jakarta.persistence.*;
import lombok.Builder;

@Builder
@Entity
@Table(name = "user_emails", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "email"})
})
public class UserEmailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userEmailId")
    private Long userEmailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private UserEntity user;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "isPrimary", nullable = false)
    private boolean isPrimary;

    @Column(name = "isVerified")
    private Boolean isVerified;

    @Column(name = "sourceIdentityId")
    private Long sourceIdentityId; // 소셜 계정에서 온 이메일이면 해당 socialUserId(optional)

}


