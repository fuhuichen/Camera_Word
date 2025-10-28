package com.example.cameracloud.web.api;

import com.example.cameracloud.entity.Camera;
import com.example.cameracloud.service.CameraService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cameras")
public class CameraApiController {
    
    private final CameraService cameraService;
    
    public CameraApiController(CameraService cameraService) {
        this.cameraService = cameraService;
    }
    
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importCameras(
            @RequestParam("file") MultipartFile file,
            @RequestParam("platformCode") String platformCode,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "請選擇檔案");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!file.getOriginalFilename().endsWith(".xlsx")) {
                response.put("success", false);
                response.put("message", "只支援 .xlsx 格式的檔案");
                return ResponseEntity.badRequest().body(response);
            }
            
            // TODO: 實現Excel解析和相機匯入邏輯
            // 這裡需要添加Excel解析功能
            
            response.put("success", true);
            response.put("message", "匯入成功");
            response.put("importedCount", 0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "匯入失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/{cameraId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleCamera(
            @PathVariable String cameraId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String action = request.get("action");
            Camera camera = cameraService.findByPublicId(cameraId);
            
            if (camera == null) {
                response.put("success", false);
                response.put("message", "找不到指定的相機");
                return ResponseEntity.notFound().build();
            }
            
            if ("enable".equals(action)) {
                cameraService.updateStatus(cameraId, Camera.CameraStatus.ACTIVE);
                response.put("message", "相機已啟用");
            } else if ("disable".equals(action)) {
                cameraService.updateStatus(cameraId, Camera.CameraStatus.DISABLED);
                response.put("message", "相機已停用");
            } else {
                response.put("success", false);
                response.put("message", "無效的操作");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "操作失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @DeleteMapping("/{cameraId}")
    public ResponseEntity<Map<String, Object>> deleteCamera(
            @PathVariable String cameraId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Camera camera = cameraService.findByPublicId(cameraId);
            
            if (camera == null) {
                response.put("success", false);
                response.put("message", "找不到指定的相機");
                return ResponseEntity.notFound().build();
            }
            
            cameraService.deleteByPublicId(cameraId);
            
            response.put("success", true);
            response.put("message", "相機已刪除");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "刪除失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/bulk-delete")
    public ResponseEntity<Map<String, Object>> bulkDeleteCameras(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String platformCode = (String) request.get("platformCode");
            String status = (String) request.get("status");
            
            // TODO: 實現批量刪除邏輯
            // 這裡需要添加批量刪除功能
            
            response.put("success", true);
            response.put("message", "批量刪除完成");
            response.put("deletedCount", 0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "批量刪除失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCameras(
            @RequestParam(required = false) String platformCode,
            Authentication authentication) {
        
        try {
            // TODO: 實現Excel匯出功能
            // 這裡需要添加Excel匯出邏輯
            
            byte[] excelData = new byte[0]; // 暫時返回空數據
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=cameras.xlsx")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(excelData);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
