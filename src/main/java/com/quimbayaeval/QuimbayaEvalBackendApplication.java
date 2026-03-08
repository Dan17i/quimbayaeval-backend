package com.quimbayaeval;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Aplicación principal de QuimbayaEVAL Backend
 */
@SpringBootApplication
@EnableCaching
public class QuimbayaEvalBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuimbayaEvalBackendApplication.class, args);
    }
}
