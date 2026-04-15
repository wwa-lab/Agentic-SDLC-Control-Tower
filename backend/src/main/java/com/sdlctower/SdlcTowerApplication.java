package com.sdlctower;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SdlcTowerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SdlcTowerApplication.class, args);
    }
}
