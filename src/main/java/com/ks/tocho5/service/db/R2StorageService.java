package com.ks.tocho5.service.db;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

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

    public String uploadPlayerPhoto(Long teamId, MultipartFile file) throws IOException {
        String extension = getExtension(file.getOriginalFilename());
        String key = "teams/" + teamId + "/players/" + UUID.randomUUID() + extension;

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        r2Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));

        return publicBaseUrl + "/" + key;
    }

    // 👉 NUEVO: subir fotos del carrusel del equipo
    public String uploadTeamCarouselPhoto(Long teamId, MultipartFile file) throws IOException {
        String extension = getExtension(file.getOriginalFilename());
        String key = "teams/" + teamId + "/carousel/" + UUID.randomUUID() + extension;

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        r2Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));

        return publicBaseUrl + "/" + key;
    }

    // 👉 NUEVO: borrar archivo en R2 a partir de la URL pública
    public void deleteByUrl(String url) {
        if (url == null || url.isBlank()) {
            return;
        }

        // Nos aseguramos de que la URL empieza con tu base pública
        String prefix = publicBaseUrl.endsWith("/") ? publicBaseUrl : publicBaseUrl + "/";
        if (!url.startsWith(prefix)) {
            // No reconocemos el formato, mejor no borrar nada
            return;
        }

        // Extraemos el "key" real en el bucket (lo que va después de publicBaseUrl/)
        String key = url.substring(prefix.length());

        DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        r2Client.deleteObject(deleteReq);
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot == -1) return "";
        return filename.substring(dot);
    }
}
