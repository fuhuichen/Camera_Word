package com.example.cameracloud.integration;

import com.example.cameracloud.entity.Camera;
import com.example.cameracloud.repository.CameraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class CameraViewIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private CameraRepository cameraRepository;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @BeforeEach
    void setUp() {
        // Clear Redis
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        
        // Create test camera
        Camera camera = new Camera("TEST_001");
        camera.setRedirectEnabled(true);
        cameraRepository.save(camera);
    }
    
    @Test
    void testCameraView_FirstRequest_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/view")
                .param("camera_id", "TEST_001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(header().string("Cache-Control", "no-store, no-cache, must-revalidate"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("TEST_001")));
    }
    
    @Test
    void testCameraView_SecondRequestWithinWindow_ShouldReturn429() throws Exception {
        // First request - should succeed
        mockMvc.perform(get("/view")
                .param("camera_id", "TEST_001"))
                .andExpect(status().isOk());
        
        // Second request within 60 seconds - should be rate limited
        mockMvc.perform(get("/view")
                .param("camera_id", "TEST_001"))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Too Many Requests")));
    }
    
    @Test
    void testCameraView_InvalidCameraId_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/view")
                .param("camera_id", "invalid..id"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Bad Request")));
    }
    
    @Test
    void testCameraView_NonExistentCamera_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/view")
                .param("camera_id", "NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Camera not found")));
    }
    
    @Test
    void testCameraView_RedirectDisabled_ShouldReturn403() throws Exception {
        // Update camera to disable redirect
        Camera camera = cameraRepository.findByPublicId("TEST_001").orElseThrow();
        camera.setRedirectEnabled(false);
        cameraRepository.save(camera);
        
        mockMvc.perform(get("/view")
                .param("camera_id", "TEST_001"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Camera stream is currently disabled")));
    }
}
