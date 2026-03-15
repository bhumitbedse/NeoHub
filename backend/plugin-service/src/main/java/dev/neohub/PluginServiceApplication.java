package dev.neohub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PluginServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PluginServiceApplication.class, args);
    }
}
