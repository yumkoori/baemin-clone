package com.sist.baemin.user.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Builder
@Getter
@EnableJpaAuditing
@Entity
@Table(
        name = "socialUser",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "providerId"}),
                @UniqueConstraint(columnNames = {"userId", "provider"})
        }
)
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "socialUserId")
    private Long socialUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private UserEntity user;

    @Column(name = "provider", nullable = false)
    private String provider;        //enum으로 변경 가능성

    @Column(name = "providerId", nullable = false)
    private String providerId;

    @Column(name = "providerEmail")
    private String providerEmail;

    @Column(name = "emailVerified")
    private Boolean emailVerified;

    @CreatedDate
    @Column(name = "linkedAt")
    private java.time.LocalDateTime linkedAt;
}
