package com.labndbnb.landbnb;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class LandbnbApplication {

    /** Todas las fechas (LocalDateTime) se calculan y guardan en UTC, sin depender de la zona del servidor. */
    @PostConstruct
    void useUtc() {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(LandbnbApplication.class, args);
    }
}
