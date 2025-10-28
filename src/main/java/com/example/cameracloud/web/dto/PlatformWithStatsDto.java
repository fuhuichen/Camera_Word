package com.example.cameracloud.web.dto;

import com.example.cameracloud.entity.Platform;

public class PlatformWithStatsDto {
    private Platform platform;
    private int deviceCount;
    private int activeDeviceCount;
    
    public PlatformWithStatsDto(Platform platform, int deviceCount, int activeDeviceCount) {
        this.platform = platform;
        this.deviceCount = deviceCount;
        this.activeDeviceCount = activeDeviceCount;
    }
    
    // Getters
    public Platform getPlatform() {
        return platform;
    }
    
    public int getDeviceCount() {
        return deviceCount;
    }
    
    public int getActiveDeviceCount() {
        return activeDeviceCount;
    }
    
    // Delegate methods to platform
    public String getCode() {
        return platform.getCode();
    }
    
    public String getName() {
        return platform.getName();
    }
    
    public String getDescription() {
        return platform.getDescription();
    }
    
    public String getWebsiteUrl() {
        return platform.getWebsiteUrl();
    }
    
    public String getTestEndpoint() {
        return platform.getTestEndpoint();
    }
    
    public Platform.PlatformStatus getStatus() {
        return platform.getStatus();
    }
}
