package com.sist.baemin.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sist.baemin.user.domain.CouponEntity;

@Repository
public interface CouponRepository extends JpaRepository<CouponEntity, Long> {
}


