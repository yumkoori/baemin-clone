package com.sist.baemin.user.controller;

import com.sist.baemin.user.service.ProfileImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileImageController {

    private final ProfileImageService profile;

    // 업로드 또는 교체
    @PostMapping("/{email:.+}/profile-image")
    public ResponseEntity<?> upload(@PathVariable String email, @RequestParam("profileImage") MultipartFile profileImage) throws Exception {
        var result = profile.uploadOrReplace(email, profileImage);
        return ResponseEntity.ok(result); // {key, url}
    }

    // 현재 이미지 URL
    @GetMapping("/{email:.+}/profile-image")
    public ResponseEntity<?> get(@PathVariable String email) {
        String url = profile.currentUrl(email);
        if (url == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(new UrlResp(url));
    }

    // 삭제
    @DeleteMapping("/{email:.+}/profile-image")
    public ResponseEntity<?> delete(@PathVariable String email) {
        profile.delete(email);
        return ResponseEntity.noContent().build();
    }

    public record UrlResp(String url) {}
}
