package com.example.cameracloud.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @org.hibernate.annotations.CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.OffsetDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @NotBlank
    @Column(nullable = false)
    private String action;
    
    @NotBlank
    @Column(name = "resource_type", nullable = false)
    private String resourceType;
    
    @NotBlank
    @Column(name = "resource_id", nullable = false)
    private String resourceId;
    
    @Column(columnDefinition = "JSONB")
    private String details;
    
    @Column(name = "ip_address", columnDefinition = "INET")
    private String ipAddress;
    
    public AuditLog() {}
    
    public AuditLog(User user, String action, String resourceType, String resourceId, String details, String ipAddress) {
        this.user = user;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.details = details;
        this.ipAddress = ipAddress;
    }
    
    // Getters and setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public java.time.OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.time.OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
