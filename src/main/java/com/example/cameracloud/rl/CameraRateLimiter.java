package com.example.cameracloud.rl;

/**
 * Rate limiter interface for camera view requests.
 * Allows only the first successful request per camera_id within a rolling time window.
 */
public interface CameraRateLimiter {
    
    /**
     * Attempts to acquire a rate limit permit for the given camera ID.
     * 
     * @param cameraId the camera ID to check rate limit for
     * @return true if the request is allowed (first in window), false if rate limited
     */
    boolean tryAcquire(String cameraId);
    
    /**
     * Gets the rate limit window duration in seconds.
     * 
     * @return window duration in seconds
     */
    int getWindowSeconds();
}
