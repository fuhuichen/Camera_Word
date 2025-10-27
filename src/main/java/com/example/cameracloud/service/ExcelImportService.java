package com.example.cameracloud.service;

import com.example.cameracloud.entity.Camera;
import com.example.cameracloud.entity.ImportJob;
import com.example.cameracloud.entity.ImportJobError;
import com.example.cameracloud.entity.Platform;
import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelImportService.class);
    
    private final CameraService cameraService;
    private final PlatformService platformService;
    
    public ExcelImportService(CameraService cameraService, PlatformService platformService) {
        this.cameraService = cameraService;
        this.platformService = platformService;
    }
    
    public ImportResult processExcelFile(ImportJob job, MultipartFile file) throws IOException {
        List<ImportJobError> errors = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                totalRows++;
                int rowNumber = i + 1; // 1-based row number
                
                try {
                    CameraData cameraData = parseExcelRow(row);
                    validateCameraData(cameraData, rowNumber);
                    
                    Camera camera = createOrUpdateCamera(cameraData);
                    cameraService.save(camera);
                    successCount++;
                    
                } catch (Exception e) {
                    logger.warn("Error processing row {}: {}", rowNumber, e.getMessage());
                    errors.add(new ImportJobError(job, rowNumber, 
                        getCellValue(row.getCell(0)), e.getMessage()));
                }
            }
        }
        
        return new ImportResult(totalRows, successCount, errors);
    }
    
    public ImportResult processCsvFile(ImportJob job, MultipartFile file) throws IOException {
        List<ImportJobError> errors = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] header = null;
            try {
                header = reader.readNext(); // Skip header
            } catch (com.opencsv.exceptions.CsvValidationException e) {
                throw new RuntimeException("CSV validation error", e);
            }
            String[] row;
            
            try {
                while ((row = reader.readNext()) != null) {
                totalRows++;
                int rowNumber = totalRows + 1; // 1-based row number
                
                    try {
                        CameraData cameraData = parseCsvRow(row);
                        validateCameraData(cameraData, rowNumber);
                        
                        Camera camera = createOrUpdateCamera(cameraData);
                        cameraService.save(camera);
                        successCount++;
                        
                    } catch (Exception e) {
                        logger.warn("Error processing CSV row {}: {}", rowNumber, e.getMessage());
                        errors.add(new ImportJobError(job, rowNumber, 
                            row.length > 0 ? row[0] : "", e.getMessage()));
                    }
                }
            } catch (com.opencsv.exceptions.CsvValidationException e) {
                throw new RuntimeException("CSV validation error", e);
            }
        }
        
        return new ImportResult(totalRows, successCount, errors);
    }
    
    private CameraData parseExcelRow(Row row) {
        String cameraId = getCellValue(row.getCell(0));
        String platformCode = getCellValue(row.getCell(1));
        String model = getCellValue(row.getCell(2));
        String status = getCellValue(row.getCell(3));
        
        return new CameraData(cameraId, platformCode, model, status);
    }
    
    private CameraData parseCsvRow(String[] row) {
        String cameraId = row.length > 0 ? row[0] : "";
        String platformCode = row.length > 1 ? row[1] : "";
        String model = row.length > 2 ? row[2] : "";
        String status = row.length > 3 ? row[3] : "";
        
        return new CameraData(cameraId, platformCode, model, status);
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    private void validateCameraData(CameraData data, int rowNumber) {
        if (data.cameraId() == null || data.cameraId().trim().isEmpty()) {
            throw new IllegalArgumentException("Camera ID is required");
        }
        
        if (!data.cameraId().matches("^[A-Za-z0-9_-]{3,128}$")) {
            throw new IllegalArgumentException("Invalid camera ID format");
        }
        
        if (data.platformCode() != null && !data.platformCode().trim().isEmpty()) {
            Platform platform = platformService.findByCode(data.platformCode());
            if (platform == null) {
                throw new IllegalArgumentException("Platform not found: " + data.platformCode());
            }
        }
        
        if (data.status() != null && !data.status().trim().isEmpty()) {
            try {
                Camera.CameraStatus.valueOf(data.status().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + data.status());
            }
        }
    }
    
    private Camera createOrUpdateCamera(CameraData data) {
        Camera existingCamera = cameraService.findByPublicId(data.cameraId());
        
        if (existingCamera != null) {
            // Update existing camera
            if (data.platformCode() != null && !data.platformCode().trim().isEmpty()) {
                existingCamera.setTargetPlatformCode(data.platformCode());
            }
            if (data.model() != null && !data.model().trim().isEmpty()) {
                existingCamera.setModel(data.model());
            }
            if (data.status() != null && !data.status().trim().isEmpty()) {
                existingCamera.setStatus(Camera.CameraStatus.valueOf(data.status().toUpperCase()));
            }
            return existingCamera;
        } else {
            // Create new camera
            Camera camera = new Camera(data.cameraId());
            camera.setModel(data.model());
            camera.setTargetPlatformCode(data.platformCode());
            
            if (data.status() != null && !data.status().trim().isEmpty()) {
                camera.setStatus(Camera.CameraStatus.valueOf(data.status().toUpperCase()));
            }
            
            return camera;
        }
    }
    
    public record CameraData(String cameraId, String platformCode, String model, String status) {}
    
    public record ImportResult(int totalRows, int successCount, List<ImportJobError> errors) {}
}
