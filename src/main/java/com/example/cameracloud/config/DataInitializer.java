package com.example.cameracloud.config;

import com.example.cameracloud.entity.Camera;
import com.example.cameracloud.entity.Platform;
import com.example.cameracloud.entity.User;
import com.example.cameracloud.repository.CameraRepository;
import com.example.cameracloud.repository.PlatformRepository;
import com.example.cameracloud.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private PlatformRepository platformRepository;
    
    @Autowired
    private CameraRepository cameraRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing development data...");
        
        // Create platforms
        createPlatforms();
        
        // Create users
        createUsers();
        
        // Create test cameras
        createTestCameras();
        
        logger.info("Development data initialization completed");
    }
    
    private void createPlatforms() {
        if (platformRepository.count() == 0) {
            // DK Platform
            Platform dk = new Platform();
            dk.setCode("dk");
            dk.setName("DK");
            dk.setDescription("DK 平台 - 专业直播平台");
            dk.setWebsiteUrl("https://dk.example.com");
            dk.setTestEndpoint("https://dk.example.com/test");
            dk.setStatus(Platform.PlatformStatus.ACTIVE);
            platformRepository.save(dk);
            
            // 兑心 Platform
            Platform duixin = new Platform();
            duixin.setCode("duixin");
            duixin.setName("兑心");
            duixin.setDescription("兑心平台 - 智能监控平台");
            duixin.setWebsiteUrl("https://duixin.example.com");
            duixin.setTestEndpoint("https://duixin.example.com/test");
            duixin.setStatus(Platform.PlatformStatus.ACTIVE);
            platformRepository.save(duixin);
            
            logger.info("Created platforms: DK, 兑心");
        }
    }
    
    private void createUsers() {
        if (userRepository.count() == 0) {
            // Main Admin
            User admin = new User();
            admin.setEmail("admin@example.com");
            admin.setDisplayName("System Administrator");
            admin.setRole(User.UserRole.MAIN_ADMIN);
            userRepository.save(admin);
            
            // Platform Admin
            User platformAdmin = new User();
            platformAdmin.setEmail("platform@example.com");
            platformAdmin.setDisplayName("Platform Administrator");
            platformAdmin.setRole(User.UserRole.PLATFORM_ADMIN);
            userRepository.save(platformAdmin);
            
            logger.info("Created users: admin, platform_admin");
        }
    }
    
    private void createTestCameras() {
        if (cameraRepository.count() == 0) {
            // DK Test Camera
            Camera dkTestCam = new Camera();
            dkTestCam.setPublicId("DK_TEST_CAM");
            dkTestCam.setModel("DK 测试相机");
            dkTestCam.setTargetPlatformCode("dk");
            dkTestCam.setStatus(Camera.CameraStatus.ACTIVE);
            dkTestCam.setRedirectEnabled(true);
            dkTestCam.setIsTestDevice(true);
            cameraRepository.save(dkTestCam);
            
            // 兑心 Test Camera
            Camera duixinTestCam = new Camera();
            duixinTestCam.setPublicId("DUIXIN_TEST_CAM");
            duixinTestCam.setModel("兑心测试相机");
            duixinTestCam.setTargetPlatformCode("duixin");
            duixinTestCam.setStatus(Camera.CameraStatus.ACTIVE);
            duixinTestCam.setRedirectEnabled(true);
            duixinTestCam.setIsTestDevice(true);
            cameraRepository.save(duixinTestCam);
            
            logger.info("Created test cameras: DK_TEST_CAM, DUIXIN_TEST_CAM");
        }
    }
}
