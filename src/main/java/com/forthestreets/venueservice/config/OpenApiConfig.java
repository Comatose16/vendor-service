package com.forthestreets.venueservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${spring.application.name}") String appName) {
        return new OpenAPI()
                .info(new Info()
                        .title("For The Streets - Venue & Specials API")
                        .version("1.0.0")
                        .description("Hyper-local discovery engine driving foot traffic across venues.")
                        .license(new License().name("Proprietary")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://sandbox.forthestreets.app").description("Sandbox Environment"),
                        new Server().url("https://api.forthestreets.app").description("Production")
                ));
    }
}