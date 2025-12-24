package com.example.cameracloud.config;

import com.example.cameracloud.entity.Camera;
import com.example.cameracloud.entity.Platform;
import com.example.cameracloud.repository.CameraRepository;
import com.example.cameracloud.repository.PlatformRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 相机初始化器 - 在所有环境下运行
 * 如果数据库无数据（相机数量为0），则创建一台测试相机
 */
@Component
@Order(2) // 在 DataInitializer (Order=1) 之后运行
public class CameraInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(CameraInitializer.class);
    
    @Autowired
    private PlatformRepository platformRepository;
    
    @Autowired
    private CameraRepository cameraRepository;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Checking camera initialization...");
        
        // Check if there are any cameras at all
        long cameraCount = cameraRepository.count();
        logger.info("Current camera count: {}", cameraCount);
        
        if (cameraCount == 0) {
            // Get all platforms
            List<Platform> allPlatforms = platformRepository.findAll();
            
            if (allPlatforms.isEmpty()) {
                logger.warn("No platforms found, skipping test camera creation");
                return;
            }
            
            // 如果数据库无数据，只创建一台测试相机（使用第一个平台）
            Platform firstPlatform = allPlatforms.get(0);
            String platformCode = firstPlatform.getCode();
            String cameraId = "TEST_CAM_001";
            
            // Check if camera already exists (to avoid duplicates)
            if (!cameraRepository.existsByPublicId(cameraId)) {
                try {
                    Camera testCam = new Camera();
                    testCam.setPublicId(cameraId);
                    testCam.setModel("測試相機");
                    testCam.setTargetPlatformCode(platformCode);
                    testCam.setStatus(Camera.CameraStatus.ACTIVE);
                    testCam.setRedirectEnabled(true);
                    testCam.setIsTestDevice(false); // 设置为 false，这样可以在列表中显示
                    
                    Camera saved = cameraRepository.save(testCam);
                    
                    // Flush to ensure data is persisted
                    cameraRepository.flush();
                    
                    // Verify the camera was saved
                    long finalCount = cameraRepository.count();
                    boolean exists = cameraRepository.existsByPublicId(cameraId);
                    
                    logger.info("Created test camera: {} for platform: {}", cameraId, firstPlatform.getName());
                    logger.info("Camera saved with ID: {}, exists check: {}, total cameras: {}", 
                               saved.getId(), exists, finalCount);
                } catch (Exception e) {
                    logger.error("Failed to create test camera: {}", e.getMessage(), e);
                    throw e;
                }
            } else {
                logger.info("Test camera {} already exists", cameraId);
            }
        } else {
            logger.info("Cameras already exist (count: {}), skipping test camera creation", cameraCount);
        }
    }
}

