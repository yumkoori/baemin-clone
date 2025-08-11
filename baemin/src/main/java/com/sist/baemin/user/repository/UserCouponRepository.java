package com.sist.baemin.user.repository;

import com.sist.baemin.user.domain.UserCouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCouponEntity, Long> {
    long countByUser_EmailAndIsUsed(String email, boolean isUsed);

}
