package com.shopping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import shopping.config.ShoppingProperties;



@SpringBootApplication(
        scanBasePackages = {
                "com.shopping",
                "shopping"
        }
)
@EnableConfigurationProperties(ShoppingProperties.class)
public class SpringAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiApplication.class, args);
    }
}