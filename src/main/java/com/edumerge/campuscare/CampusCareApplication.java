package com.edumerge.campuscare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CampusCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusCareApplication.class, args);
    }
}
