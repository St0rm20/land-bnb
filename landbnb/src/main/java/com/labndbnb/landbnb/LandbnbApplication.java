package com.labndbnb.landbnb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LandbnbApplication {
    public static void main(String[] args) {
        SpringApplication.run(LandbnbApplication.class, args);
    }
}
