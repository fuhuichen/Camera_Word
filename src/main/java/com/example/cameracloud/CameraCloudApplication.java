package com.example.cameracloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CameraCloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(CameraCloudApplication.class, args);
    }
}
