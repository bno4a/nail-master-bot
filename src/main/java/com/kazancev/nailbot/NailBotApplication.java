package com.kazancev.nailbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NailBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(NailBotApplication.class, args);
    }
}
