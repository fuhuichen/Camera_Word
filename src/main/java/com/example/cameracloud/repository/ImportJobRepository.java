package com.example.cameracloud.repository;

import com.example.cameracloud.entity.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImportJobRepository extends JpaRepository<ImportJob, UUID> {
}
