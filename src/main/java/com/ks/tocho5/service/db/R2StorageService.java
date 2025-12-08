// src/main/java/com/ks/tocho5/service/storage/R2StorageService.java
package com.ks.tocho5.service.db;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class R2StorageService {

    private final S3Client r2Client;

    @Value("${r2.bucketName}")
    private String bucketName;

    @Value("${r2.publicBaseUrl}")
    private String publicBaseUrl;

    public R2StorageService(S3Client r2Client) {
        this.r2Client = r2Client;
    }

    public String uploadTeamLogo(Long teamId, MultipartFile file) throws IOException {
        String extension = getExtension(file.getOriginalFilename());
        String key = "teams/" + teamId + "/logo-" + UUID.randomUUID() + extension;

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        r2Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));

        // URL pública que guardarás en Postgres
        return publicBaseUrl + "/" + key;
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot == -1) return "";
        return filename.substring(dot);
    }
}
