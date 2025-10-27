package com.example.cameracloud.web;

import com.example.cameracloud.entity.Camera;
import com.example.cameracloud.rl.CameraRateLimiter;
import com.example.cameracloud.service.CameraService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ViewControllerTest {
    
    private CameraRateLimiter rateLimiter;
    private CameraService cameraService;
    private ViewController viewController;
    private HttpServletRequest request;
    
    @BeforeEach
    void setUp() {
        rateLimiter = mock(CameraRateLimiter.class);
        cameraService = mock(CameraService.class);
        viewController = new ViewController(rateLimiter, cameraService);
        request = new MockHttpServletRequest();
    }
    
    @Test
    void testViewCamera_ValidCameraIdAndAllowed_ShouldReturn200() {
        // Given
        String cameraId = "CAMERA_001";
        Camera camera = new Camera(cameraId);
        camera.setRedirectEnabled(true);
        
        when(cameraService.findByPublicId(cameraId)).thenReturn(camera);
        when(rateLimiter.tryAcquire(cameraId)).thenReturn(true);
        
        // When
        ResponseEntity<String> response = viewController.viewCamera(cameraId, request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains(cameraId));
        assertTrue(response.getHeaders().containsKey("Cache-Control"));
        assertTrue(response.getHeaders().containsKey("X-Frame-Options"));
    }
    
    @Test
    void testViewCamera_RateLimited_ShouldReturn429() {
        // Given
        String cameraId = "CAMERA_001";
        Camera camera = new Camera(cameraId);
        camera.setRedirectEnabled(true);
        
        when(cameraService.findByPublicId(cameraId)).thenReturn(camera);
        when(rateLimiter.tryAcquire(cameraId)).thenReturn(false);
        
        // When
        ResponseEntity<String> response = viewController.viewCamera(cameraId, request);
        
        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertTrue(response.getBody().contains("Too Many Requests"));
    }
    
    @Test
    void testViewCamera_CameraNotFound_ShouldReturn404() {
        // Given
        String cameraId = "NONEXISTENT";
        when(cameraService.findByPublicId(cameraId)).thenReturn(null);
        
        // When
        ResponseEntity<String> response = viewController.viewCamera(cameraId, request);
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("Camera not found"));
    }
    
    @Test
    void testViewCamera_RedirectDisabled_ShouldReturn403() {
        // Given
        String cameraId = "CAMERA_001";
        Camera camera = new Camera(cameraId);
        camera.setRedirectEnabled(false);
        
        when(cameraService.findByPublicId(cameraId)).thenReturn(camera);
        
        // When
        ResponseEntity<String> response = viewController.viewCamera(cameraId, request);
        
        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().contains("Camera stream is currently disabled"));
    }
}
