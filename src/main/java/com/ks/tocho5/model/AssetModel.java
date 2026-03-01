package com.ks.tocho5.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ks.tocho5.model.dto.AssetStatusENUM;

@Entity
@Table(name = "assets")
public class AssetModel {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "r2_bucket", nullable = false)
    private String r2Bucket;

    @Column(name = "r2_key", nullable = false, unique = true)
    private String r2Key;

    @Column(name = "public_url", nullable = false)
    private String publicUrl;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssetStatusENUM status = AssetStatusENUM.ACTIVE;

    @Column(name = "created_by")
    private String createdBy;

    // Estos los llena Postgres por default + trigger
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public AssetModel() {}

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID();
    }

    // getters/setters (te dejo lo esencial)
    public UUID getId() { return id; }
    public String getR2Bucket() { return r2Bucket; }
    public void setR2Bucket(String r2Bucket) { this.r2Bucket = r2Bucket; }
    public String getR2Key() { return r2Key; }
    public void setR2Key(String r2Key) { this.r2Key = r2Key; }
    public String getPublicUrl() { return publicUrl; }
    public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public AssetStatusENUM getStatus() { return status; }
    public void setStatus(AssetStatusENUM status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}