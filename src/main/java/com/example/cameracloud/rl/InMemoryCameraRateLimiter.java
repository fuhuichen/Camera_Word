package com.example.cameracloud.rl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Profile("dev")
public class InMemoryCameraRateLimiter implements CameraRateLimiter {
    
    private static final Logger logger = LoggerFactory.getLogger(InMemoryCameraRateLimiter.class);
    
    private final ConcurrentHashMap<String, Instant> rateLimitMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    private final int windowSeconds;
    
    public InMemoryCameraRateLimiter(@Value("${app.rate.window-seconds:60}") int windowSeconds) {
        this.windowSeconds = windowSeconds;
        
        // Cleanup expired entries every minute
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.MINUTES);
        
        logger.warn("Using in-memory rate limiter - NOT suitable for production multi-instance deployments");
    }
    
    @Override
    public synchronized boolean tryAcquire(String cameraId) {
        Instant now = Instant.now();
        Instant lastAccess = rateLimitMap.get(cameraId);
        
        if (lastAccess == null || now.isAfter(lastAccess.plusSeconds(windowSeconds))) {
            // First request or window expired
            rateLimitMap.put(cameraId, now);
            logger.info("Rate limit allow for camera_id={}", cameraId);
            return true;
        } else {
            // Within rate limit window
            logger.info("Rate limit blocked for camera_id={}", cameraId);
            return false;
        }
    }
    
    @Override
    public int getWindowSeconds() {
        return windowSeconds;
    }
    
    private void cleanupExpiredEntries() {
        Instant cutoff = Instant.now().minusSeconds(windowSeconds);
        rateLimitMap.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
    }
    
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}
