package com.sist.baemin.user.service;

import com.sist.baemin.user.domain.WishlistsEntity;
import com.sist.baemin.user.dto.FavoriteRequestDto;
import com.sist.baemin.user.repository.WishlistRepository;
import com.sist.baemin.user.repository.UserRepository;
import com.sist.baemin.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WishlistDbService {
    
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    
    /**
     * 찜 목록에 추가
     * @param userId 사용자 ID
     * @param request 찜 추가 요청 데이터
     * @return 생성된 찜 ID
     */
    public Long addToWishlist(Long userId, FavoriteRequestDto request) {
        log.info("찜 추가 시작 - userId: {}, targetId: {}, type: {}", 
                userId, request.getTargetId(), request.getType());
        
        // store 타입만 지원
        if (!"store".equals(request.getType())) {
            throw new RuntimeException("현재는 가게 찜만 지원합니다.");
        }
        
        // 이미 찜한 가게인지 확인
        Optional<WishlistsEntity> existing = wishlistRepository
                .findByUserIdAndStoreId(userId, request.getTargetId());
        
        if (existing.isPresent()) {
            log.info("이미 찜한 가게 - wishlistId: {}", existing.get().getWishlistId());
            return existing.get().getWishlistId();
        }
        
        WishlistsEntity wishlist = new WishlistsEntity();
        wishlist.setUser(userRepository.findById(userId).orElseThrow(() -> 
            new RuntimeException("사용자를 찾을 수 없습니다: " + userId)));
        wishlist.setStore(storeRepository.findById(request.getTargetId()).orElseThrow(() -> 
            new RuntimeException("가게를 찾을 수 없습니다: " + request.getTargetId())));
        wishlist.setCreatedAt(LocalDateTime.now());
        
        WishlistsEntity saved = wishlistRepository.save(wishlist);
        log.info("찜 추가 완료 - wishlistId: {}", saved.getWishlistId());
        
        return saved.getWishlistId();
    }
    
    /**
     * 찜 목록에서 삭제
     * @param userId 사용자 ID
     * @param wishlistId 찜 ID
     */
    public void removeFromWishlist(Long userId, Long wishlistId) {
        log.info("찜 삭제 - userId: {}, wishlistId: {}", userId, wishlistId);
        
        WishlistsEntity wishlist = wishlistRepository.findById(wishlistId)
            .orElseThrow(() -> new RuntimeException("찜 항목을 찾을 수 없습니다: " + wishlistId));
        
        // 본인의 찜인지 확인
        if (!wishlist.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
        
        wishlistRepository.delete(wishlist);
        log.info("찜 삭제 완료 - wishlistId: {}", wishlistId);
    }
    
    /**
     * 사용자별 찜 목록 조회
     * @param userId 사용자 ID
     * @param targetType 타겟 타입 ("store", "menu" 등)
     * @return 찜 목록
     */
    @Transactional(readOnly = true)
    public List<WishlistsEntity> getWishlistsByUser(Long userId, String targetType) {
        log.info("찜 목록 조회 - userId: {}, targetType: {}", userId, targetType);
        // 현재는 store만 지원하므로 targetType 무시하고 모든 찜 조회
        return wishlistRepository.findByUserId(userId);
    }
    
    /**
     * 찜 상태 확인
     * @param userId 사용자 ID
     * @param targetType 타겟 타입
     * @param targetId 타겟 ID
     * @return 찜 엔티티 (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<WishlistsEntity> checkWishlistStatus(Long userId, String targetType, Long targetId) {
        if ("store".equals(targetType)) {
            return wishlistRepository.findByUserIdAndStoreId(userId, targetId);
        }
        return Optional.empty();
    }
    
    /**
     * 사용자의 전체 찜 개수 조회
     * @param userId 사용자 ID
     * @param targetType 타겟 타입
     * @return 찜 개수
     */
    @Transactional(readOnly = true)
    public long getWishlistCount(Long userId, String targetType) {
        return getWishlistsByUser(userId, targetType).size();
    }
}