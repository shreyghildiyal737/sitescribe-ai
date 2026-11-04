package com.sitescribe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.sitescribe.config.AiProperties;

@SpringBootApplication
@EnableConfigurationProperties(AiProperties.class)
public class SiteScribeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SiteScribeApplication.class, args);
    }
}
