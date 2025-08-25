package com.sist.baemin.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3;

    @Value("${aws.s3.bucket}") private String bucket;
    @Value("${aws.s3.public-base-url}") private String publicBaseUrl;

    public void put(String key, MultipartFile file) throws Exception {
        String contentType = file.getContentType() != null
                ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .cacheControl("public, max-age=31536000")
                .build();

        try (InputStream in = file.getInputStream()) {
            s3.putObject(req, RequestBody.fromInputStream(in, file.getSize()));
        }
    }

    public void deleteQuietly(String key) {
        if (key == null || key.isBlank()) return;
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (Exception ignore) {}
    }

    public String toUrl(String key) {
        if (key == null || key.isBlank()) return null;
        if (key.startsWith("http")) return key;       // 이미 URL이면 그대로
        if (key.startsWith("/"))  return key;         // /images/... 같은 정적경로 레거시
        return publicBaseUrl + "/" + encodePath(key);       // S3 키를 퍼블릭 URL로
    }

    private static String encodePath(String key) {
        return Arrays.stream(key.split("/"))
                .map(seg -> URLEncoder.encode(seg, StandardCharsets.UTF_8).replace("+", "%20"))
                .collect(Collectors.joining("/"));
    }

}
