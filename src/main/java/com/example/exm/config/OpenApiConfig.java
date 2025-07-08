package com.example.exm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .servers(List.of(new Server().url(contextPath)))
        .info(
            new Info()
                .title("EXM Application API")
                .description("REST API for EXM - Product, Order, and User Management System")
                .version("1.0.0")
                .contact(
                    new Contact()
                        .name("Development Team")
                        .email("dev@example.com")
                        .url("https://example.com"))
                .license(
                    new License().name("MIT License").url("https://opensource.org/licenses/MIT")));
  }
}
