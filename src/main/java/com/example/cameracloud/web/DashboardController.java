package com.example.cameracloud.web;

import com.example.cameracloud.entity.Camera;
import com.example.cameracloud.entity.Platform;
import com.example.cameracloud.repository.CameraRepository;
import com.example.cameracloud.service.CameraService;
import com.example.cameracloud.service.PlatformService;
import com.example.cameracloud.web.dto.PlatformWithStatsDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import java.util.Map;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dashboard and account management controller
 */
@Controller
public class DashboardController {
    
    private final PlatformService platformService;
    private final CameraService cameraService;
    private final CameraRepository cameraRepository;
    
    public DashboardController(PlatformService platformService, CameraService cameraService, CameraRepository cameraRepository) {
        this.platformService = platformService;
        this.cameraService = cameraService;
        this.cameraRepository = cameraRepository;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .collect(Collectors.joining(", "));
        
        // Get platform statistics
        List<Platform> allPlatforms = platformService.findAll();
        List<Platform> activePlatforms = platformService.findAllActive();
        
        // Get camera statistics
        List<Camera> allCameras = cameraService.findWithFilters(null, null, null, null);
        List<Camera> activeCameras = cameraService.findWithFilters(null, Camera.CameraStatus.ACTIVE, null, null);
        List<Camera> disabledCameras = cameraService.findWithFilters(null, Camera.CameraStatus.DISABLED, null, null);
        
        // Calculate statistics
        int totalPlatforms = allPlatforms.size();
        int activePlatformCount = activePlatforms.size();
        int totalCameras = allCameras.size();
        int activeCameraCount = activeCameras.size();
        int disabledCameraCount = disabledCameras.size();
        
        // Calculate uptime percentage
        double uptimePercentage = totalCameras > 0 ? (double) activeCameraCount / totalCameras * 100 : 0;
        
        // Get current time
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // Add attributes to model
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("totalPlatforms", totalPlatforms);
        model.addAttribute("activePlatformCount", activePlatformCount);
        model.addAttribute("totalCameras", totalCameras);
        model.addAttribute("activeCameraCount", activeCameraCount);
        model.addAttribute("disabledCameraCount", disabledCameraCount);
        model.addAttribute("uptimePercentage", String.format("%.1f", uptimePercentage));
        model.addAttribute("currentTime", currentTime);
        model.addAttribute("platforms", activePlatforms);
        model.addAttribute("recentCameras", activeCameras.stream().limit(5).collect(Collectors.toList()));
        
        return "dashboard";
    }
    
    @GetMapping("/api/debug/platforms")
    public ResponseEntity<?> debugPlatformsApi(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        // Get platform information
        List<Platform> allPlatforms = platformService.findAll();
        List<Camera> allCameras = cameraRepository.findAll();
        
        // Create platform DTOs with statistics
        List<PlatformWithStatsDto> platformsWithStats = allPlatforms.stream()
                .map(platform -> {
                    long deviceCount = allCameras.stream()
                            .filter(camera -> platform.getCode().equals(camera.getTargetPlatformCode()))
                            .count();
                    long activeDeviceCount = allCameras.stream()
                            .filter(camera -> platform.getCode().equals(camera.getTargetPlatformCode()) 
                                    && camera.getStatus() == Camera.CameraStatus.ACTIVE)
                            .count();
                    return new PlatformWithStatsDto(platform, (int) deviceCount, (int) activeDeviceCount);
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "platforms", platformsWithStats,
            "platformCount", platformsWithStats.size(),
            "allPlatforms", allPlatforms,
            "allCameras", allCameras
        ));
    }
    @GetMapping("/platforms")
    public String platforms(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .collect(Collectors.joining(", "));
        
        // Get platform information
        List<Platform> allPlatforms = platformService.findAll();
        List<Platform> activePlatforms = platformService.findAllActive();
        
        // Get camera statistics for each platform
        List<Camera> allCameras = cameraService.findWithFilters(null, null, null, null);
        List<Camera> activeCameras = cameraService.findWithFilters(null, Camera.CameraStatus.ACTIVE, null, null);
        
        // Create platform DTOs with statistics
        List<PlatformWithStatsDto> platformsWithStats = allPlatforms.stream()
                .map(platform -> {
                    long deviceCount = allCameras.stream()
                            .filter(camera -> platform.getCode().equals(camera.getTargetPlatformCode()))
                            .count();
                    long activeDeviceCount = activeCameras.stream()
                            .filter(camera -> platform.getCode().equals(camera.getTargetPlatformCode()))
                            .count();
                    return new PlatformWithStatsDto(platform, (int) deviceCount, (int) activeDeviceCount);
                })
                .collect(Collectors.toList());
        
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("platforms", platformsWithStats);
        model.addAttribute("activePlatforms", activePlatforms);
        model.addAttribute("totalCameras", allCameras.size());
        model.addAttribute("activeCameras", activeCameras.size());
        
        return "platforms";
    }
    
    @GetMapping("/dashboard/platform")
    public String dashboardPlatform(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .collect(Collectors.joining(", "));
        
        // Get platform information
        List<Platform> allPlatforms = platformService.findAll();
        List<Platform> activePlatforms = platformService.findAllActive();
        
        // Get camera statistics for each platform
        List<Camera> allCameras = cameraService.findWithFilters(null, null, null, null);
        List<Camera> activeCameras = cameraService.findWithFilters(null, Camera.CameraStatus.ACTIVE, null, null);
        
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("platforms", allPlatforms);
        model.addAttribute("activePlatforms", activePlatforms);
        model.addAttribute("totalCameras", allCameras.size());
        model.addAttribute("activeCameras", activeCameras.size());
        
        return "platforms";
    }
    
    @GetMapping("/platforms/{platformCode}")
    public String platformDetail(Authentication authentication, Model model, 
                                @PathVariable String platformCode) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .collect(Collectors.joining(", "));
        
        // Get platform information
        Platform platform = platformService.findByCode(platformCode);
        if (platform == null) {
            return "redirect:/platforms";
        }
        
        // Get cameras for this platform
        List<Camera> platformCameras = cameraService.findWithFilters(platformCode, null, null, null);
        List<Camera> activeCameras = cameraService.findWithFilters(platformCode, Camera.CameraStatus.ACTIVE, null, null);
        
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("platform", platform);
        model.addAttribute("platformCameras", platformCameras);
        model.addAttribute("activeCameras", activeCameras);
        model.addAttribute("totalCameras", platformCameras.size());
        model.addAttribute("activeCameraCount", activeCameras.size());
        
        return "platform-detail";
    }
    
    @GetMapping("/api/platforms")
    public ResponseEntity<List<Platform>> getPlatformsApi() {
        List<Platform> platforms = platformService.findAll();
        return ResponseEntity.ok(platforms);
    }
}