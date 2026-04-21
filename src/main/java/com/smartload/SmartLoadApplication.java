package com.smartload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SmartLoadApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartLoadApplication.class, args);
    }
}
