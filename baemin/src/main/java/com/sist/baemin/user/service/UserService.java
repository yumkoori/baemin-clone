package com.sist.baemin.user.service;

import com.sist.baemin.common.util.JwtUtil;
import com.sist.baemin.user.domain.KaKaoUserInfo;
import com.sist.baemin.user.domain.UserEntity;
import com.sist.baemin.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    public String processKaKaoUserLogin(KaKaoUserInfo userInfo) {
        String email = userInfo.getKakao_account().getEmail();
        Long targetId = userInfo.getId();
        Optional<UserEntity> user = userRepository.findByEmail(email);

        if(user.isPresent()) {
            System.out.println("가입된 회원입니다.");
        } else {
            System.out.println("회원가입 처리 시작");
            UserEntity userEntity = UserEntity.builder().email(email).build();
            userRepository.save(userEntity);
        }
        return jwtUtil.generateTokenForKaKao(email, targetId);
    }
}
