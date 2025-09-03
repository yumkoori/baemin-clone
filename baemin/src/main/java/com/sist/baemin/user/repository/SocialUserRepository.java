package com.sist.baemin.user.repository;

import com.sist.baemin.user.domain.SocialUserEntity;
import com.sist.baemin.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialUserRepository extends JpaRepository<SocialUserEntity, Long> {
    @Query("select s.user from SocialUserEntity s where s.provider = 'kakao' and s.providerEmail = :email")
    Optional<UserEntity> findKakaoUserByProviderEmail(@Param("email") String email);

    // provider(google/kakao 등)와 providerId(sub, 카카오 id 등)로 찾기
    Optional<SocialUserEntity> findByProviderAndProviderId(String provider, String providerId);
}


