package com.sist.baemin.user.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sist.baemin.user.domain.UserCouponEntity;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCouponEntity, Long> {
    List<UserCouponEntity> findByExpiresAtBefore(LocalDateTime cutoff);
}


