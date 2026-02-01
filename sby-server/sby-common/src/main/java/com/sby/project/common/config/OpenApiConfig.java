package com.sby.project.common.config;

//import io.swagger.v3.oas.models.Components;
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Info;
//import io.swagger.v3.oas.models.security.SecurityRequirement;
//import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

//    @Bean
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .info(new Info()
//                        .title("sby-admin API 文档")
//                        .version("1.0")
//                        .description("基于 Spring Boot 3 + SpringDoc 的接口文档"))
//                // 配置全局鉴权（Header 里的 Authorization）
//                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
//                .components(new Components()
//                        .addSecuritySchemes("BearerAuth",
//                                new SecurityScheme()
//                                        .name("Authorization")
//                                        .type(SecurityScheme.Type.HTTP)
//                                        .scheme("bearer")
//                                        .bearerFormat("JWT")));
//    }
}