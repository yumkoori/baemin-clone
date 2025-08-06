package com.sist.baemin.user.service;

import com.sist.baemin.user.domain.KaKaoUserInfo;
import com.sist.baemin.user.domain.UserEntity;
import com.sist.baemin.user.dto.UserDTO;
import com.sist.baemin.user.repository.UserRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserDTO processKaKaoUserLogin(KaKaoUserInfo userInfo) {
        String email = userInfo.getKakao_account().getEmail();
        Optional<UserEntity> user = userRepository.findByEmail(email);

        if(user.isPresent()) {
            UserEntity userEntity = user.get();
            return UserDTO.builder()
                    .userId(userEntity.getUserId())
                    .email(userEntity.getEmail())
                    .build();
        } else {
            UserEntity userEntity = UserEntity.builder().email(email).build();
            userRepository.save(userEntity);

            return UserDTO.builder()
                    .email(userEntity.getEmail())
                    .build();
        }
    }
}
