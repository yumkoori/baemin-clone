package com.sist.baemin.user.service;

import com.sist.baemin.user.domain.UserEntity;
import com.sist.baemin.user.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private final UserRepository users;
    private final S3Service s3;

    @Value("${app.image.allowed-types}") private String allowedTypesCsv;
    @Value("${app.image.max-size-bytes}") private long maxBytes;

    private Set<String> allowedTypes() {
        return Arrays.stream(allowedTypesCsv.split(",")).map(String::trim).collect(Collectors.toSet());
    }

    private static String extFrom(String filename) {
        if (filename == null) return ".jpg";
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i) : ".jpg";
    }

    @Transactional
    public Result uploadOrReplace(@NotNull String email, @NotNull MultipartFile file) throws Exception {
        if (file.isEmpty()) throw new IllegalArgumentException("파일이 없습니다.");
        if (file.getSize() > maxBytes) throw new IllegalArgumentException("파일 용량 초과");

        String ct = file.getContentType();
        if (!allowedTypes().contains(ct)) throw new IllegalArgumentException("허용되지 않은 타입: " + ct);

        UserEntity user = users.findByEmail(email).orElseThrow();

        String oldKey = user.getProfileImage();

        String safeEmail = email.replaceAll("[^a-zA-Z0-9._-]", "_");
        String key = "users/%s/profile/%s%s".formatted(
                safeEmail,
                UUID.randomUUID(),
                extFrom(file.getOriginalFilename())
        );

        s3.put(key, file);          // 1) 업로드
        user.setProfileImage(key);
        users.save(user);           // 2) DB 갱신
        s3.deleteQuietly(oldKey);   // 3) 이전 파일 정리

        return new Result(key, s3.toUrl(key));
    }

    @Transactional
    public void delete(@NotNull String email) {
        UserEntity user = users.findByEmail(email).orElseThrow();
        s3.deleteQuietly(user.getProfileImage());
        user.setProfileImage(null);
        users.save(user);
    }

    public String currentUrl(@NotNull String email) {
        UserEntity user = users.findByEmail(email).orElseThrow();
        return user.getProfileImage() == null ? null : s3.toUrl(user.getProfileImage());
    }

    public record Result(String key, String url) {}
}
