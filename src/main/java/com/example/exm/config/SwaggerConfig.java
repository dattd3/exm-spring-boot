package com.example.exm.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.awt.Desktop;
import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig implements CommandLineRunner {

    private final Environment environment;

    @Override
    public void run(String... args) throws Exception {
        String port = environment.getProperty("server.port", "8080");
        String contextPath = environment.getProperty("server.servlet.context-path", "/");
        String swaggerPath = environment.getProperty("springdoc.swagger-ui.path", "/swagger-ui.html");

        String url = String.format("http://localhost:%s%s%s", port, contextPath, swaggerPath);

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
        }
    }
}