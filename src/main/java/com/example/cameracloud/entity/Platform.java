package com.example.cameracloud.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;

@Entity
@Table(name = "platforms")
public class Platform {
    
    @Id
    @NotBlank
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-z0-9_-]+$", message = "Platform code must contain only lowercase letters, numbers, underscores, and hyphens")
    private String code;
    
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlatformStatus status = PlatformStatus.ACTIVE;
    
    @Column(name = "test_endpoint")
    private String testEndpoint;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "website_url")
    private String websiteUrl;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    
    public Platform() {}
    
    public Platform(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    // Getters and setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public PlatformStatus getStatus() {
        return status;
    }
    
    public void setStatus(PlatformStatus status) {
        this.status = status;
    }
    
    public String getTestEndpoint() {
        return testEndpoint;
    }
    
    public void setTestEndpoint(String testEndpoint) {
        this.testEndpoint = testEndpoint;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
    
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public enum PlatformStatus {
        ACTIVE, DISABLED
    }
}
