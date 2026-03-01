package com.ks.tocho5.service.db;

import com.ks.tocho5.model.AssetModel;
import com.ks.tocho5.model.dto.AssetDTO;
import com.ks.tocho5.model.dto.AssetStatusENUM;
import com.ks.tocho5.repository.AssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class AssetService {

    private static final Set<String> ALLOWED_CT = Set.of("image/png", "image/jpeg", "image/webp");

    // ✅ Igual que tu R2StorageService (permite mayúsculas también)
    private static final Pattern SAFE_FOLDER = Pattern.compile("^[a-zA-Z0-9/_-]+$");

    private final R2StorageService r2;
    private final AssetRepository repo;

    public AssetService(R2StorageService r2, AssetRepository repo) {
        this.r2 = r2;
        this.repo = repo;
    }

    public AssetDTO uploadPublicImage(String folder, MultipartFile file, String createdBy) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Archivo vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CT.contains(contentType)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido: " + contentType);
        }

        if (folder == null || folder.isBlank() || !SAFE_FOLDER.matcher(folder).matches()) {
            throw new IllegalArgumentException("Folder inválido");
        }

        // ✅ Aquí ya no armamos key nosotros; lo hace R2StorageService
        R2StorageService.UploadResult up = r2.uploadSiteAsset(folder, file);

        AssetModel a = new AssetModel();
        // Si tu AssetModel tiene r2Bucket, lo dejamos con default de DB o lo seteamos (ver opción B)
        // a.setR2Bucket(r2.getBucketName());

        a.setR2Key(up.key());
        a.setPublicUrl(up.publicUrl());

        a.setContentType(contentType);
        a.setSizeBytes(file.getSize());
        a.setOriginalName(file.getOriginalFilename());

        a.setCreatedBy(createdBy);
        a.setStatus(AssetStatusENUM.ACTIVE);

        AssetModel saved = repo.save(a);

        return new AssetDTO(
                saved.getId(),
                saved.getPublicUrl(),
                saved.getR2Key(),
                saved.getContentType(),
                saved.getSizeBytes()
        );
    }
}