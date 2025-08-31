package com.sist.baemin.user.service;

import com.sist.baemin.user.domain.UserAddressEntity;
import com.sist.baemin.user.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAddressService {
    
    private final UserAddressRepository userAddressRepository;
    
    // 사용자 ID를 기반으로 기본 주소를 조회하는 메소드
    public UserAddressEntity findDefaultAddressByUserId(Long userId) {
        System.out.println("=== UserAddressService.findDefaultAddressByUserId() called with userId: " + userId + " ===");
        List<UserAddressEntity> addresses = userAddressRepository.findByUser_UserId(userId);
        System.out.println("Addresses found for user: " + addresses);
        
        if (addresses != null && !addresses.isEmpty()) {
            System.out.println("Number of addresses found: " + addresses.size());
            // isDefault가 true인 주소를 찾습니다.
            for (UserAddressEntity address : addresses) {
                System.out.println("Checking address - ID: " + address.getAddressId() + ", isDefault: " + address.isDefault() + 
                                 ", lat: " + address.getLatitude() + ", lon: " + address.getLongitude());
                if (address.isDefault()) {
                    System.out.println("Found default address: " + address.getAddressId());
                    return address;
                }
            }
            // isDefault가 true인 주소가 없으면 첫 번째 주소를 반환합니다.
            UserAddressEntity firstAddress = addresses.get(0);
            System.out.println("No default address found, returning first address: " + firstAddress.getAddressId());
            return firstAddress;
        }
        System.out.println("No addresses found for user, returning null");
        return null;
    }
    
    // 사용자 ID를 기반으로 모든 주소를 조회하는 메소드
    public List<UserAddressEntity> findAllAddressesByUserId(Long userId) {
        return userAddressRepository.findByUser_UserId(userId);
    }
}