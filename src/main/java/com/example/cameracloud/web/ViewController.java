package com.example.cameracloud.web;

import com.example.cameracloud.entity.Camera;
import com.example.cameracloud.rl.CameraRateLimiter;
import com.example.cameracloud.service.CameraService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@Validated
public class ViewController {
    
    private static final Logger logger = LoggerFactory.getLogger(ViewController.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final CameraRateLimiter rateLimiter;
    private final CameraService cameraService;
    
    public ViewController(CameraRateLimiter rateLimiter, CameraService cameraService) {
        this.rateLimiter = rateLimiter;
        this.cameraService = cameraService;
    }
    
    @GetMapping(value = "/view", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> viewCamera(
            @RequestParam("camera_id") 
            @NotBlank(message = "Camera ID is required")
            @Size(min = 3, max = 128, message = "Camera ID must be between 3 and 128 characters")
            @Pattern(regexp = "^[A-Za-z0-9_-]{3,128}$", message = "Camera ID must contain only alphanumeric characters, underscores, and hyphens")
            String cameraId,
            HttpServletRequest request) {
        
        String remoteIp = getClientIpAddress(request);
        
        try {
            // Check if camera exists and redirect is enabled
            Camera camera = cameraService.findByPublicId(cameraId);
            if (camera == null) {
                logger.info("Camera not found: camera_id={}, remote_ip={}", cameraId, remoteIp);
                return createErrorResponse("Camera not found", HttpStatus.NOT_FOUND);
            }
            
            if (!camera.getRedirectEnabled()) {
                logger.info("Camera redirect disabled: camera_id={}, remote_ip={}", cameraId, remoteIp);
                return createErrorResponse("Camera stream is currently disabled", HttpStatus.FORBIDDEN);
            }
            
            // Check rate limit
            boolean allowed = rateLimiter.tryAcquire(cameraId);
            
            if (allowed) {
                logger.info("Camera view allowed: camera_id={}, remote_ip={}, outcome=ALLOW", cameraId, remoteIp);
                return createSuccessResponse(cameraId);
            } else {
                logger.info("Camera view rate limited: camera_id={}, remote_ip={}, outcome=RATE_LIMIT", cameraId, remoteIp);
                return createRateLimitResponse();
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error in camera view: camera_id={}, remote_ip={}", cameraId, remoteIp, e);
            return createErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    private ResponseEntity<String> createSuccessResponse(String cameraId) {
        String html = String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="robots" content="noindex,nofollow">
                <title>Camera View - %s</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .camera-info { background: #e3f2fd; padding: 20px; border-radius: 4px; margin: 20px 0; }
                    .time { color: #666; font-size: 14px; }
                    h1 { color: #1976d2; margin-bottom: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>📹 Camera Stream</h1>
                    <div class="camera-info">
                        <strong>Camera ID:</strong> %s<br>
                        <strong>Status:</strong> Active<br>
                        <strong>Server Time:</strong> <span class="time">%s</span>
                    </div>
                    <p>Camera stream placeholder - actual streaming implementation would go here.</p>
                </div>
            </body>
            </html>
            """, cameraId, cameraId, OffsetDateTime.now().format(TIME_FORMATTER));
        
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store, no-cache, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Frame-Options", "DENY")
                .header("Referrer-Policy", "no-referrer")
                .body(html);
    }
    
    private ResponseEntity<String> createRateLimitResponse() {
        String html = String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="robots" content="noindex,nofollow">
                <title>Too Many Requests</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .error { background: #ffebee; padding: 20px; border-radius: 4px; margin: 20px 0; color: #c62828; }
                    h1 { color: #c62828; margin-bottom: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🚫 Too Many Requests</h1>
                    <div class="error">
                        <strong>Rate Limit Exceeded</strong><br>
                        此 camera_id 在 %d 秒內已被使用，請稍後再試。
                    </div>
                </div>
            </body>
            </html>
            """, rateLimiter.getWindowSeconds());
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(html);
    }
    
    private ResponseEntity<String> createErrorResponse(String message, HttpStatus status) {
        String html = String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="robots" content="noindex,nofollow">
                <title>%s</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .error { background: #ffebee; padding: 20px; border-radius: 4px; margin: 20px 0; color: #c62828; }
                    h1 { color: #c62828; margin-bottom: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>❌ %s</h1>
                    <div class="error">
                        %s
                    </div>
                </div>
            </body>
            </html>
            """, message, message, message);
        
        return ResponseEntity.status(status).body(html);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
