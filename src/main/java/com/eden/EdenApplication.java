package com.eden;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EdenApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdenApplication.class, args);
    }
}
