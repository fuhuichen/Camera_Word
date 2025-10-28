package com.example.cameracloud.web.admin;

import com.example.cameracloud.service.ImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/cameras")
@PreAuthorize("hasRole('MAIN_ADMIN') or hasRole('PLATFORM_ADMIN')")
public class CameraImportController {
    
    private final ImportService importService;
    
    public CameraImportController(ImportService importService) {
        this.importService = importService;
    }
    
    @PostMapping("/import")
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<ImportJobResponse> importCameras(@RequestParam("file") MultipartFile file) {
        UUID jobId = importService.processImportFile(file);
        return ResponseEntity.ok(new ImportJobResponse(jobId));
    }
    
    @GetMapping("/import/{jobId}")
    public ResponseEntity<ImportService.ImportJobStatusResponse> getImportStatus(@PathVariable UUID jobId) {
        ImportService.ImportJobStatusResponse response = importService.getImportStatus(jobId);
        return ResponseEntity.ok(response);
    }
    
    public record ImportJobResponse(UUID jobId) {}
}
