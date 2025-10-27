package com.example.cameracloud.service;

import com.example.cameracloud.entity.Camera;
import com.example.cameracloud.repository.CameraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CameraService {
    
    private final CameraRepository cameraRepository;
    
    public CameraService(CameraRepository cameraRepository) {
        this.cameraRepository = cameraRepository;
    }
    
    public Camera findByPublicId(String publicId) {
        return cameraRepository.findByPublicId(publicId).orElse(null);
    }
    
    public boolean existsByPublicId(String publicId) {
        return cameraRepository.existsByPublicId(publicId);
    }
    
    public List<Camera> findWithFilters(String platformCode, 
                                       Camera.CameraStatus status, 
                                       Boolean redirectEnabled, 
                                       String search) {
        return cameraRepository.findWithFilters(platformCode, status, redirectEnabled, search);
    }
    
    public List<Camera> findByTargetPlatformCode(String platformCode) {
        return cameraRepository.findByTargetPlatformCode(platformCode);
    }
    
    public List<Camera> findByTargetPlatformCodeIn(List<String> platformCodes) {
        return cameraRepository.findByTargetPlatformCodeIn(platformCodes);
    }
    
    @Transactional
    public Camera save(Camera camera) {
        return cameraRepository.save(camera);
    }
    
    @Transactional
    public void updateRedirectEnabled(String publicId, boolean enabled) {
        Optional<Camera> cameraOpt = cameraRepository.findByPublicId(publicId);
        if (cameraOpt.isPresent()) {
            Camera camera = cameraOpt.get();
            camera.setRedirectEnabled(enabled);
            cameraRepository.save(camera);
        }
    }
    
    @Transactional
    public void updateTargetPlatform(String publicId, String platformCode) {
        Optional<Camera> cameraOpt = cameraRepository.findByPublicId(publicId);
        if (cameraOpt.isPresent()) {
            Camera camera = cameraOpt.get();
            camera.setTargetPlatformCode(platformCode);
            cameraRepository.save(camera);
        }
    }
}
