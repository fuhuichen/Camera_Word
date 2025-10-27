package com.example.cameracloud.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "import_job_errors")
public class ImportJobError {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @org.hibernate.annotations.CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.OffsetDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @NotNull
    private ImportJob job;
    
    @Column(name = "row_no")
    private Integer rowNo;
    
    @Column(name = "camera_id_in_file")
    private String cameraIdInFile;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    public ImportJobError() {}
    
    public ImportJobError(ImportJob job, Integer rowNo, String cameraIdInFile, String errorMessage) {
        this.job = job;
        this.rowNo = rowNo;
        this.cameraIdInFile = cameraIdInFile;
        this.errorMessage = errorMessage;
    }
    
    // Getters and setters
    public ImportJob getJob() {
        return job;
    }
    
    public void setJob(ImportJob job) {
        this.job = job;
    }
    
    public Integer getRowNo() {
        return rowNo;
    }
    
    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
    }
    
    public String getCameraIdInFile() {
        return cameraIdInFile;
    }
    
    public void setCameraIdInFile(String cameraIdInFile) {
        this.cameraIdInFile = cameraIdInFile;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public java.time.OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.time.OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
