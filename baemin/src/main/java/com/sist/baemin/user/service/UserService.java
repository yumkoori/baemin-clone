package com.sist.baemin.user.service;

import com.sist.baemin.common.util.JwtUtil;
import com.sist.baemin.user.domain.KaKaoUserInfo;
import com.sist.baemin.user.domain.UserAddressEntity;
import com.sist.baemin.user.domain.UserEntity;
import com.sist.baemin.user.dto.*;
import com.sist.baemin.user.repository.UserAddressRepository;
import com.sist.baemin.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserAddressRepository userAddressRepository;
    @Autowired
    private JwtUtil jwtUtil;
    public String processKaKaoUserLogin(KaKaoUserInfo userInfo, String kakaoAccessToken) {
        String email = userInfo.getKakao_account().getEmail();
        Long targetId = userInfo.getId();

        Optional<UserEntity> userOpt = userRepository.findByEmail(email);

        if(userOpt.isPresent()) {
            System.out.println("가입된 회원입니다: " + email);
            // 기존 회원의 정보 업데이트 (닉네임이 변경되었을 수 있음)
            UserEntity existingUser = userOpt.get();
            userRepository.save(existingUser);
        } else {
            System.out.println("회원가입 처리 시작: " + email);
            UserEntity userEntity = UserEntity.builder()
                    .email(email)
                    .nickname("배민이")
                    .name(userOpt.get().getName())
                    .role("USER")
                    .tier("BRONZE")
                    .createdAt(java.time.LocalDateTime.now())
                    .build();
            userRepository.save(userEntity);
        }
        return jwtUtil.generateTokenForKaKao(email, targetId, kakaoAccessToken);
    }


}
