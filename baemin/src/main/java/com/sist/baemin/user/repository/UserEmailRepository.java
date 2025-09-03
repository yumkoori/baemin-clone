package com.sist.baemin.user.repository;

import com.sist.baemin.user.domain.UserEmailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEmailRepository extends JpaRepository<UserEmailEntity, Long> {
    Optional<UserEmailEntity> findByEmail(String email);

}


