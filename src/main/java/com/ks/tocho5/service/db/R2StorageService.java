package com.ks.tocho5.service.db;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class R2StorageService {

    private final S3Client r2Client;

    @Value("${r2.bucketName}")
    private String bucketName;

    @Value("${r2.publicBaseUrl}")
    private String publicBaseUrl;

    // Para evitar folder raros tipo "../" o cosas con espacios
    private static final Pattern SAFE_FOLDER = Pattern.compile("^[a-zA-Z0-9/_-]+$");

    public R2StorageService(S3Client r2Client) {
        this.r2Client = r2Client;
    }

    /**
     * Resultado útil para guardar en Postgres (assets):
     * - key: el objeto real en R2 (r2_key)
     * - publicUrl: la URL pública final (public_url)
     */
    public record UploadResult(String key, String publicUrl) {}

    // ============================
    // ✅ TUS MÉTODOS ORIGINALES (no se rompen)
    // ============================

    public String uploadTeamLogo(Long teamId, MultipartFile file) throws IOException {
        String extension = resolveExtension(file);
        String key = "teams/" + teamId + "/logo-" + UUID.randomUUID() + extension;

        return uploadByKey(key, file).publicUrl();
    }

    public String uploadPlayerPhoto(Long teamId, MultipartFile file) throws IOException {
        String extension = resolveExtension(file);
        String key = "teams/" + teamId + "/players/" + UUID.randomUUID() + extension;

        return uploadByKey(key, file).publicUrl();
    }

    // 👉 NUEVO (ya lo tenías): subir fotos del carrusel del equipo
    public String uploadTeamCarouselPhoto(Long teamId, MultipartFile file) throws IOException {
        String extension = resolveExtension(file);
        String key = "teams/" + teamId + "/carousel/" + UUID.randomUUID() + extension;

        return uploadByKey(key, file).publicUrl();
    }

    // 👉 NUEVO (ya lo tenías): borrar archivo en R2 a partir de la URL pública
    public void deleteByUrl(String url) {
        String key = extractKeyFromPublicUrl(url);
        if (key == null) return;

        DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        r2Client.deleteObject(deleteReq);
    }

    // ============================
    // ✅ LO NUEVO QUE TE FALTABA (para site/home + tabla assets)
    // ============================

    /**
     * Sube una imagen pública para el sitio (home, sponsors, etc.)
     * Ejemplo folder: "site/home/hero" o "site/home/sponsors/dicass"
     * Devuelve key + publicUrl (listo para guardar en tabla assets).
     */
    public UploadResult uploadSiteAsset(String folder, MultipartFile file) throws IOException {
        if (folder == null || folder.isBlank()) {
            throw new IllegalArgumentException("folder es requerido");
        }
        if (!SAFE_FOLDER.matcher(folder).matches()) {
            throw new IllegalArgumentException("folder inválido (solo letras, números, /, _, -)");
        }

        String extension = resolveExtension(file);
        String key = trimSlashes(folder) + "/" + UUID.randomUUID() + extension;

        return uploadByKey(key, file);
    }

    /**
     * Borra directamente por key (más útil cuando ya guardas r2_key en Postgres).
     */
    public void deleteByKey(String key) {
        if (key == null || key.isBlank()) return;

        DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        r2Client.deleteObject(deleteReq);
    }

    // ============================
    // ✅ IMPLEMENTACIÓN CENTRAL (no duplica código)
    // ============================

    /**
     * Este método hace la subida real a R2.
     * ✅ Importante: usa streaming (no file.getBytes()) para no reventar RAM.
     */
    private UploadResult uploadByKey(String key, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Archivo vacío");
        }

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                // Opcional: cache fuerte para imágenes públicas (ajústalo a tu gusto)
                // .cacheControl("public, max-age=31536000, immutable")
                .build();

        // ✅ Streaming: evita cargar todo en memoria
        r2Client.putObject(
                putReq,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        String url = normalizeBaseUrl(publicBaseUrl) + "/" + key;
        return new UploadResult(key, url);
    }

    // ============================
    // Helpers
    // ============================

    private String extractKeyFromPublicUrl(String url) {
        if (url == null || url.isBlank()) return null;

        String prefix = normalizeBaseUrl(publicBaseUrl) + "/";
        if (!url.startsWith(prefix)) {
            // No coincide con tu base pública, mejor no borrar nada
            return null;
        }
        return url.substring(prefix.length());
    }

    private String normalizeBaseUrl(String base) {
        if (base == null) return "";
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    private String trimSlashes(String s) {
        String x = s;
        while (x.startsWith("/")) x = x.substring(1);
        while (x.endsWith("/")) x = x.substring(0, x.length() - 1);
        return x;
    }

    /**
     * Usa la extensión del filename si existe; si no, usa el content-type como fallback.
     */
    private String resolveExtension(MultipartFile file) {
        String ext = getExtension(file != null ? file.getOriginalFilename() : null);
        if (ext != null && !ext.isBlank()) return ext;

        String ct = file != null ? file.getContentType() : null;
        if (ct == null) return "";

        return switch (ct) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot == -1) return "";
        return filename.substring(dot);
    }
}