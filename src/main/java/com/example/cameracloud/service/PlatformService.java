package com.example.cameracloud.service;

import com.example.cameracloud.entity.Camera;
import com.example.cameracloud.entity.Platform;
import com.example.cameracloud.repository.CameraRepository;
import com.example.cameracloud.repository.PlatformRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlatformService {
    
    private final PlatformRepository platformRepository;
    private final CameraRepository cameraRepository;
    
    public PlatformService(PlatformRepository platformRepository, CameraRepository cameraRepository) {
        this.platformRepository = platformRepository;
        this.cameraRepository = cameraRepository;
    }
    
    public List<Platform> findAllActive() {
        return platformRepository.findByStatus(Platform.PlatformStatus.ACTIVE);
    }
    
    public List<Platform> findAll() {
        return platformRepository.findAll();
    }
    
    public Platform findByCode(String code) {
        return platformRepository.findById(code).orElse(null);
    }
    
    @Transactional
    public Platform createPlatform(String code, String name) {
        Platform platform = new Platform(code, name);
        return platformRepository.save(platform);
    }
    
    @Transactional
    public Platform updatePlatform(String code, UpdatePlatformRequest request) {
        Platform platform = platformRepository.findById(code)
            .orElseThrow(() -> new IllegalArgumentException("Platform not found: " + code));
        
        if (request.name() != null) {
            platform.setName(request.name());
        }
        if (request.status() != null) {
            platform.setStatus(request.status());
        }
        if (request.testEndpoint() != null) {
            platform.setTestEndpoint(request.testEndpoint());
        }
        
        return platformRepository.save(platform);
    }
    
    public PlatformTestResponse testPlatform(String code) {
        Platform platform = platformRepository.findById(code)
            .orElseThrow(() -> new IllegalArgumentException("Platform not found: " + code));
        
        // Simple test - in real implementation, this would test the actual platform connectivity
        if (platform.getStatus() == Platform.PlatformStatus.ACTIVE) {
            return PlatformTestResponse.ok("Platform is active and accessible");
        } else {
            return PlatformTestResponse.fail("Platform is disabled");
        }
    }
    
    public boolean existsByCode(String code) {
        return platformRepository.existsById(code);
    }
    
    public PlatformInfoResponse getPlatformInfo(String code) {
        Platform platform = platformRepository.findById(code)
            .orElseThrow(() -> new IllegalArgumentException("Platform not found: " + code));
        
        // Get device count for this platform
        Long deviceCount = cameraRepository.countActiveDevicesByPlatform(code);
        
        // Get test device for this platform
        List<Camera> testDevices = cameraRepository.findTestDevicesByPlatform(code);
        String testDeviceId = testDevices.isEmpty() ? null : testDevices.get(0).getPublicId();
        
        return new PlatformInfoResponse(
            platform.getCode(),
            platform.getName(),
            platform.getDescription(),
            platform.getWebsiteUrl(),
            platform.getTestEndpoint(),
            deviceCount.intValue(),
            testDeviceId
        );
    }
    
    public record PlatformInfoResponse(
        String code,
        String name,
        String description,
        String websiteUrl,
        String testEndpoint,
        Integer deviceCount,
        String testDeviceId
    ) {}
    
    public record UpdatePlatformRequest(
        String name,
        Platform.PlatformStatus status,
        String testEndpoint
    ) {}
    
    public record PlatformTestResponse(
        boolean success,
        String message
    ) {
        public static PlatformTestResponse ok(String message) {
            return new PlatformTestResponse(true, message);
        }
        
        public static PlatformTestResponse fail(String message) {
            return new PlatformTestResponse(false, message);
        }
    }
}
