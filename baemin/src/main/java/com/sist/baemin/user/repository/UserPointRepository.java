package com.sist.baemin.user.repository;

import com.sist.baemin.user.domain.UserPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPointRepository extends JpaRepository<UserPointEntity, Long> {
    // email로 currentPoint 조회
    @Query("SELECT up.currentPoint FROM UserPointEntity up WHERE up.userid.userId = :userId")
    Long findCurrentPointByUserId(@Param("userId") Long userId);
}
