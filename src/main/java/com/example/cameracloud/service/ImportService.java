package com.example.cameracloud.service;

import com.example.cameracloud.entity.Camera;
import com.example.cameracloud.entity.ImportJob;
import com.example.cameracloud.entity.Platform;
import com.example.cameracloud.entity.User;
import com.example.cameracloud.repository.CameraRepository;
import com.example.cameracloud.repository.ImportJobRepository;
import com.example.cameracloud.repository.PlatformRepository;
import com.example.cameracloud.repository.UserRepository;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling camera import operations from Excel/CSV files.
 */
@Service
@Transactional
public class ImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImportService.class);
    
    private final CameraRepository cameraRepository;
    private final PlatformRepository platformRepository;
    private final UserRepository userRepository;
    private final ImportJobRepository importJobRepository;
    private final AuditService auditService;
    
    @Autowired
    public ImportService(CameraRepository cameraRepository,
                        PlatformRepository platformRepository,
                        UserRepository userRepository,
                        ImportJobRepository importJobRepository,
                        AuditService auditService) {
        this.cameraRepository = cameraRepository;
        this.platformRepository = platformRepository;
        this.userRepository = userRepository;
        this.importJobRepository = importJobRepository;
        this.auditService = auditService;
    }
    
    /**
     * Start import job for uploaded file.
     */
    public UUID processImportFile(MultipartFile file) {
        try {
            // Validate file
            validateImportFile(file);
            
            // Create import job with a default user (for now)
            ImportJob job = new ImportJob();
            job.setFileName(file.getOriginalFilename());
            job.setStatus(ImportJob.ImportJobStatus.QUEUED);
            
            // Get admin user as uploader
            User adminUser = userRepository.findByEmail("admin@example.com").orElse(null);
            if (adminUser != null) {
                job.setUploaderUser(adminUser);
            }
            
            job = importJobRepository.save(job);
            
            logger.info("Import job created: {} for file: {}", 
                    job.getId(), file.getOriginalFilename());
            
            // Start async processing
            processImportFileAsync(job.getId(), file);
            
            return job.getId();
        } catch (Exception e) {
            logger.error("Failed to create import job", e);
            throw new RuntimeException("Failed to create import job: " + e.getMessage());
        }
    }
    
    /**
     * Get import job status and details.
     */
    @Transactional(readOnly = true)
    public ImportJobStatusResponse getImportStatus(UUID jobId) {
        ImportJob job = importJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Import job not found: " + jobId));
        
        return new ImportJobStatusResponse(
            job.getId(),
            job.getStatus().toString(),
            job.getTotalRows(),
            job.getSuccessRows(),
            job.getFailedRows(),
            new ArrayList<>() // TODO: Load errors
        );
    }
    
    /**
     * Validate import file.
     */
    private void validateImportFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".csv"))) {
            throw new IllegalArgumentException("File must be .xlsx or .csv format");
        }
        
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
    }
    
    /**
     * Process import file asynchronously.
     */
    @Async
    public CompletableFuture<Void> processImportFileAsync(UUID jobId, MultipartFile file) {
        try {
            ImportJob job = importJobRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalArgumentException("Import job not found: " + jobId));
            
            job.setStatus(ImportJob.ImportJobStatus.PROCESSING);
            importJobRepository.save(job);
            
            List<String> errors = new ArrayList<>();
            int successCount = 0;
            int totalRows = 0;
            
            if (file.getOriginalFilename().endsWith(".xlsx")) {
                processExcelFile(file, job, errors);
            } else {
                processCsvFile(file, job, errors);
            }
            
            // Update job status
            job.setStatus(ImportJob.ImportJobStatus.DONE);
            job.setTotalRows(totalRows);
            job.setSuccessRows(successCount);
            job.setFailedRows(errors.size());
            importJobRepository.save(job);
            
            logger.info("Import job completed: {} - Success: {}, Failed: {}", 
                    jobId, successCount, errors.size());
            
        } catch (Exception e) {
            logger.error("Import job failed: " + jobId, e);
            
            ImportJob job = importJobRepository.findById(jobId).orElse(null);
            if (job != null) {
                job.setStatus(ImportJob.ImportJobStatus.FAILED);
                importJobRepository.save(job);
            }
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Process Excel file.
     */
    private void processExcelFile(MultipartFile file, ImportJob job, List<String> errors) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            int rowNum = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowNum++;
                
                // Skip header row
                if (rowNum == 1) continue;
                
                try {
                    processCameraRow(row, job, rowNum);
                } catch (Exception e) {
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Process CSV file.
     */
    private void processCsvFile(MultipartFile file, ImportJob job, List<String> errors) throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        
        try (InputStream inputStream = file.getInputStream();
             MappingIterator<Map<String, String>> iterator = mapper.readerFor(Map.class)
                     .with(schema)
                     .readValues(inputStream)) {
            
            int rowNum = 1; // Start from 1, header is row 0
            while (iterator.hasNext()) {
                rowNum++;
                Map<String, String> row = iterator.next();
                
                try {
                    processCameraRow(row, job, rowNum);
                } catch (Exception e) {
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Process camera row from Excel.
     */
    private void processCameraRow(Row row, ImportJob job, int rowNum) {
        String cameraId = getCellValueAsString(row.getCell(0));
        String model = getCellValueAsString(row.getCell(1));
        String platformCode = getCellValueAsString(row.getCell(2));
        String status = getCellValueAsString(row.getCell(3));
        
        processCameraData(cameraId, model, platformCode, status, rowNum);
    }
    
    /**
     * Process camera row from CSV.
     */
    private void processCameraRow(Map<String, String> row, ImportJob job, int rowNum) {
        String cameraId = row.get("camera_id");
        String model = row.get("model");
        String platformCode = row.get("platform_code");
        String status = row.get("status");
        
        processCameraData(cameraId, model, platformCode, status, rowNum);
    }
    
    /**
     * Process camera data and save to database.
     */
    private void processCameraData(String cameraId, String model, String platformCode, 
                                 String status, int rowNum) {
        // Validate required fields
        if (cameraId == null || cameraId.trim().isEmpty()) {
            throw new IllegalArgumentException("camera_id is required");
        }
        
        if (!cameraId.matches("^[A-Za-z0-9_-]{3,128}$")) {
            throw new IllegalArgumentException("Invalid camera_id format");
        }
        
        // Validate platform if provided
        Platform platform = null;
        if (platformCode != null && !platformCode.trim().isEmpty()) {
            platform = platformRepository.findById(platformCode.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Platform not found: " + platformCode));
        }
        
        // Check if camera already exists
        Optional<Camera> existingCamera = cameraRepository.findByPublicId(cameraId.trim());
        
        Camera camera;
        if (existingCamera.isPresent()) {
            // Update existing camera
            camera = existingCamera.get();
            if (model != null && !model.trim().isEmpty()) {
                camera.setModel(model.trim());
            }
            if (platform != null) {
                camera.setTargetPlatformCode(platformCode.trim());
            }
            if (status != null && !status.trim().isEmpty()) {
                camera.setStatus(Camera.CameraStatus.fromValue(status.trim().toLowerCase()));
            }
        } else {
            // Create new camera
            camera = new Camera();
            camera.setPublicId(cameraId.trim());
            camera.setModel(model != null ? model.trim() : null);
            camera.setTargetPlatformCode(platform != null ? platformCode.trim() : null);
            camera.setStatus(status != null && !status.trim().isEmpty() ? 
                    Camera.CameraStatus.fromValue(status.trim().toLowerCase()) : 
                    Camera.CameraStatus.ACTIVE);
        }
        
        cameraRepository.save(camera);
    }
    
    /**
     * Get cell value as string from Excel row.
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
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
                return null;
        }
    }
    
    public record ImportJobStatusResponse(UUID jobId, String status, Integer totalRows, 
                                        Integer successRows, Integer failedRows, 
                                        List<String> errors) {}
}