package com.shopping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import shopping.config.ShoppingProperties;

@SpringBootApplication(scanBasePackages = {"com.shopping", "shopping"})
@EnableConfigurationProperties(ShoppingProperties.class)
public class LangChain4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                LangChain4jApplication.class,
                args
        );
    }
}
