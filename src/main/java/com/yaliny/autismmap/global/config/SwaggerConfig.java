package com.yaliny.autismmap.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "Autism Map API 문서",
        description = "Autism Map 서비스의 API 명세서입니다.",
        version = "v1.0",
        contact = @Contact(
            name = "Yaliny 개발",
            email = "mae1217@naver.com"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8095", description = "로컬 서버")
    }
)
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);

        SecurityScheme securityScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");

        Components components = new Components().addSecuritySchemes(jwt, securityScheme);

        return new OpenAPI()
            .addSecurityItem(securityRequirement)
            .components(components);
    }
}
