package com.sist.baemin.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long userId;

    @Column(name = "email", nullable = true, unique = true)     //일단 null 허용
    private String email;

    @Column(nullable = true)            //일단 null 허용
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "role")
    private String role;        //나중에 enum으로 변경

    @Column(name = "name")
    private String name;

    @Column(name = "tier")
    private String tier;        //나중에 enum으로 변경

    @Column(name = "profileImage")
    private String profileImage;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    @CreatedDate
    @Column(name = "createdAt")
    private LocalDateTime createdAt;


}
