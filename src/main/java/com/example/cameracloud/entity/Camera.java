package com.example.cameracloud.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "cameras")
public class Camera extends BaseEntity {
    
    @NotBlank
    @Size(min = 3, max = 128)
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,128}$", message = "Camera ID must contain only alphanumeric characters, underscores, and hyphens")
    @Column(name = "public_id", unique = true, nullable = false)
    private String publicId;
    
    @Size(max = 100)
    private String model;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CameraStatus status = CameraStatus.ACTIVE;
    
    @Column(name = "target_platform_code")
    private String targetPlatformCode;
    
    @Column(name = "redirect_enabled", nullable = false)
    private Boolean redirectEnabled = true;
    
    @Column(name = "is_test_device", nullable = false)
    private Boolean isTestDevice = false;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_platform_code", insertable = false, updatable = false)
    private Platform targetPlatform;
    
    public Camera() {}
    
    public Camera(String publicId) {
        this.publicId = publicId;
    }
    
    // Getters and setters
    public String getPublicId() {
        return publicId;
    }
    
    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public CameraStatus getStatus() {
        return status;
    }
    
    public void setStatus(CameraStatus status) {
        this.status = status;
    }
    
    public String getTargetPlatformCode() {
        return targetPlatformCode;
    }
    
    public void setTargetPlatformCode(String targetPlatformCode) {
        this.targetPlatformCode = targetPlatformCode;
    }
    
    public Boolean getRedirectEnabled() {
        return redirectEnabled;
    }
    
    public void setRedirectEnabled(Boolean redirectEnabled) {
        this.redirectEnabled = redirectEnabled;
    }
    
    public Platform getTargetPlatform() {
        return targetPlatform;
    }
    
    public void setTargetPlatform(Platform targetPlatform) {
        this.targetPlatform = targetPlatform;
    }
    
    public Boolean getIsTestDevice() {
        return isTestDevice;
    }
    
    public void setIsTestDevice(Boolean isTestDevice) {
        this.isTestDevice = isTestDevice;
    }
    
    public enum CameraStatus {
        ACTIVE, DISABLED;
        
        public static CameraStatus fromValue(String value) {
            if (value == null) {
                return ACTIVE;
            }
            try {
                return CameraStatus.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid camera status: " + value + ". Must be 'active' or 'disabled'");
            }
        }
    }
}
