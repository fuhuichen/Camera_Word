package com.example.cameracloud.web;

import com.example.cameracloud.service.ImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/cameras")
public class CameraImportController {
    
    private final ImportService importService;
    
    public CameraImportController(ImportService importService) {
        this.importService = importService;
    }
    
    @PostMapping("/import")
    public ResponseEntity<ImportJobResponse> importCameras(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            UUID jobId = importService.processImportFile(file);
            return ResponseEntity.ok(new ImportJobResponse(jobId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/import/{jobId}")
    public ResponseEntity<ImportService.ImportJobStatusResponse> getImportStatus(@PathVariable UUID jobId) {
        ImportService.ImportJobStatusResponse response = importService.getImportStatus(jobId);
        return ResponseEntity.ok(response);
    }
    
    public record ImportJobResponse(UUID jobId) {}
}
