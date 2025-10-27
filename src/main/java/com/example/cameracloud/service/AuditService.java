package com.example.cameracloud.service;

import com.example.cameracloud.entity.AuditLog;
import com.example.cameracloud.entity.User;
import com.example.cameracloud.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class AuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    
    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }
    
    public void logAction(User user, String action, String resourceType, String resourceId, 
                         Map<String, Object> details, String ipAddress) {
        try {
            String detailsJson = null;
            if (details != null && !details.isEmpty()) {
                detailsJson = objectMapper.writeValueAsString(details);
            }
            
            AuditLog auditLog = new AuditLog(user, action, resourceType, resourceId, detailsJson, ipAddress);
            auditLogRepository.save(auditLog);
            
            logger.info("Audit log created: user={}, action={}, resource={}:{}", 
                user != null ? user.getEmail() : "anonymous", action, resourceType, resourceId);
                
        } catch (Exception e) {
            logger.error("Failed to create audit log", e);
        }
    }
    
    public void logAction(String action, String resourceType, String resourceId, 
                         Map<String, Object> details, String ipAddress) {
        logAction(null, action, resourceType, resourceId, details, ipAddress);
    }
    
    public void logCameraView(String cameraId, String outcome, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("outcome", outcome);
        details.put("timestamp", OffsetDateTime.now());
        
        logAction("camera_view", "camera", cameraId, details, ipAddress);
    }
    
    public void logCameraRedirectDisabled(String cameraId, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("action", "redirect_disabled_block");
        details.put("timestamp", OffsetDateTime.now());
        
        logAction("redirect_disabled_block", "camera", cameraId, details, ipAddress);
    }
    
    public void logPlatformCreated(User user, String platformCode, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("platform_code", platformCode);
        
        logAction(user, "platform_created", "platform", platformCode, details, ipAddress);
    }
    
    public void logPlatformUpdated(User user, String platformCode, Map<String, Object> changes, String ipAddress) {
        logAction(user, "platform_updated", "platform", platformCode, changes, ipAddress);
    }
    
    public void logCameraAssigned(User user, String cameraId, String platformCode, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("platform_code", platformCode);
        
        logAction(user, "camera_assigned", "camera", cameraId, details, ipAddress);
    }
    
    public void logCameraRedirectToggled(User user, String cameraId, boolean enabled, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("redirect_enabled", enabled);
        
        logAction(user, "camera_redirect_toggled", "camera", cameraId, details, ipAddress);
    }
    
    public void logImportStarted(User user, String fileName, String ipAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("file_name", fileName);
        
        logAction(user, "import_started", "import_job", "new", details, ipAddress);
    }
}
