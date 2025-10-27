package com.example.cameracloud.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "import_jobs")
public class ImportJob extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_user_id", nullable = false)
    @NotNull
    private User uploaderUser;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "total_rows")
    private Integer totalRows;
    
    @Column(name = "success_rows")
    private Integer successRows;
    
    @Column(name = "failed_rows")
    private Integer failedRows;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportJobStatus status = ImportJobStatus.QUEUED;
    
    public ImportJob() {}
    
    public ImportJob(User uploaderUser, String fileName) {
        this.uploaderUser = uploaderUser;
        this.fileName = fileName;
    }
    
    // Getters and setters
    public User getUploaderUser() {
        return uploaderUser;
    }
    
    public void setUploaderUser(User uploaderUser) {
        this.uploaderUser = uploaderUser;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public Integer getTotalRows() {
        return totalRows;
    }
    
    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }
    
    public Integer getSuccessRows() {
        return successRows;
    }
    
    public void setSuccessRows(Integer successRows) {
        this.successRows = successRows;
    }
    
    public Integer getFailedRows() {
        return failedRows;
    }
    
    public void setFailedRows(Integer failedRows) {
        this.failedRows = failedRows;
    }
    
    public ImportJobStatus getStatus() {
        return status;
    }
    
    public void setStatus(ImportJobStatus status) {
        this.status = status;
    }
    
    public enum ImportJobStatus {
        QUEUED, PROCESSING, DONE, FAILED
    }
}
