package com.sist.baemin.user.repository;

import com.sist.baemin.user.domain.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddressEntity, Long> {
    List<UserAddressEntity> findByUser_UserId(Long userId);
    void deleteByAddressIdAndUser_UserId(Long addressId, Long userId);
}
