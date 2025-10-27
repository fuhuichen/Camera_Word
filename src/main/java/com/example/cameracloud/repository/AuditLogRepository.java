package com.example.cameracloud.repository;

import com.example.cameracloud.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUserId(UUID userId);
    
    List<AuditLog> findByResourceTypeAndResourceId(String resourceType, String resourceId);
    
    List<AuditLog> findByCreatedAtAfter(OffsetDateTime since);
}
