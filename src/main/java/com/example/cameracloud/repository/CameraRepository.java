package com.example.cameracloud.repository;

import com.example.cameracloud.entity.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CameraRepository extends JpaRepository<Camera, UUID> {
    
    Optional<Camera> findByPublicId(String publicId);
    
    boolean existsByPublicId(String publicId);
    
    @Query("SELECT c FROM Camera c WHERE " +
           "(:platformCode IS NULL OR c.targetPlatformCode = :platformCode) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:redirectEnabled IS NULL OR c.redirectEnabled = :redirectEnabled) AND " +
           "(:search IS NULL OR c.publicId LIKE %:search% OR c.model LIKE %:search%) AND " +
           "c.isTestDevice = false")
    List<Camera> findWithFilters(
        @Param("platformCode") String platformCode,
        @Param("status") Camera.CameraStatus status,
        @Param("redirectEnabled") Boolean redirectEnabled,
        @Param("search") String search
    );
    
    @Query("SELECT c FROM Camera c WHERE c.targetPlatformCode = :platformCode AND c.isTestDevice = true")
    List<Camera> findTestDevicesByPlatform(@Param("platformCode") String platformCode);
    
    @Query("SELECT COUNT(c) FROM Camera c WHERE c.targetPlatformCode = :platformCode AND c.isTestDevice = false")
    Long countActiveDevicesByPlatform(@Param("platformCode") String platformCode);
    
    List<Camera> findByTargetPlatformCode(String platformCode);
    
    List<Camera> findByTargetPlatformCodeIn(List<String> platformCodes);
}
