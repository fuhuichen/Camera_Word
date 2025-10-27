package com.example.cameracloud.rl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = "app.rate.window-seconds=60")
class CameraRateLimiterTest {
    
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private RedisCameraRateLimiter rateLimiter;
    
    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        rateLimiter = new RedisCameraRateLimiter(redisTemplate, 60);
    }
    
    @Test
    void testTryAcquire_FirstRequest_ShouldAllow() {
        // Given
        String cameraId = "CAMERA_001";
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
            .thenReturn(true);
        
        // When
        boolean result = rateLimiter.tryAcquire(cameraId);
        
        // Then
        assertTrue(result);
        verify(valueOperations).setIfAbsent(
            eq("rate:view:" + cameraId), 
            eq("1"), 
            eq(Duration.ofSeconds(60))
        );
    }
    
    @Test
    void testTryAcquire_SecondRequestWithinWindow_ShouldBlock() {
        // Given
        String cameraId = "CAMERA_001";
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
            .thenReturn(false);
        
        // When
        boolean result = rateLimiter.tryAcquire(cameraId);
        
        // Then
        assertFalse(result);
        verify(valueOperations).setIfAbsent(
            eq("rate:view:" + cameraId), 
            eq("1"), 
            eq(Duration.ofSeconds(60))
        );
    }
    
    @Test
    void testTryAcquire_RedisException_ShouldAllow() {
        // Given
        String cameraId = "CAMERA_001";
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
            .thenThrow(new RuntimeException("Redis connection failed"));
        
        // When
        boolean result = rateLimiter.tryAcquire(cameraId);
        
        // Then
        assertTrue(result); // Fail open
    }
    
    @Test
    void testGetWindowSeconds() {
        assertEquals(60, rateLimiter.getWindowSeconds());
    }
}
