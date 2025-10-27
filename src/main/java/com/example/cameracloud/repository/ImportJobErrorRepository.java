package com.example.cameracloud.repository;

import com.example.cameracloud.entity.ImportJob;
import com.example.cameracloud.entity.ImportJobError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImportJobErrorRepository extends JpaRepository<ImportJobError, UUID> {
    
    List<ImportJobError> findByJob(ImportJob job);
}
