package com.example.cameracloud.repository;

import com.example.cameracloud.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformRepository extends JpaRepository<Platform, String> {
    
    List<Platform> findByStatus(Platform.PlatformStatus status);
    
    List<Platform> findByStatusAndCodeIn(Platform.PlatformStatus status, List<String> codes);
    
    boolean existsByCodeAndStatus(String code, Platform.PlatformStatus status);
}
