package com.cts.claimbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "ClaimBridge API",
                version = "1.0",
                description = "API"
        )
)
@EnableFeignClients(basePackages = "com.cts.claimbridge.client")
public class ClaimbridgeApplication {

    public static void main(String[] args) {
		SpringApplication.run(ClaimbridgeApplication.class, args);
	}

}