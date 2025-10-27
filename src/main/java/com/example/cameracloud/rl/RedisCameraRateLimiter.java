package com.example.cameracloud.rl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Profile("!dev")
public class RedisCameraRateLimiter implements CameraRateLimiter {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisCameraRateLimiter.class);
    private static final String RATE_LIMIT_KEY_PREFIX = "rate:view:";
    
    private final StringRedisTemplate redisTemplate;
    private final int windowSeconds;
    
    public RedisCameraRateLimiter(StringRedisTemplate redisTemplate,
                                  @Value("${app.rate.window-seconds:60}") int windowSeconds) {
        this.redisTemplate = redisTemplate;
        this.windowSeconds = windowSeconds;
    }
    
    @Override
    public boolean tryAcquire(String cameraId) {
        try {
            String key = RATE_LIMIT_KEY_PREFIX + cameraId;
            Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(windowSeconds));
            
            boolean allowed = Boolean.TRUE.equals(success);
            
            if (allowed) {
                logger.info("Rate limit allow for camera_id={}", cameraId);
            } else {
                logger.info("Rate limit blocked for camera_id={}", cameraId);
            }
            
            return allowed;
            
        } catch (Exception e) {
            logger.error("Redis rate limiter error for camera_id={}", cameraId, e);
            // Fail open - allow request if Redis is unavailable
            return true;
        }
    }
    
    @Override
    public int getWindowSeconds() {
        return windowSeconds;
    }
}
