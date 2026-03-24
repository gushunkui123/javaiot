package com.factorylink.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author valarchie
 * SpringDoc API文档相关配置
 */
@Configuration
@SecurityScheme(
    name = "BearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "输入 JWT token，Swagger UI 会自动附加 Bearer 前缀"
)
@OpenAPIDefinition(security = @SecurityRequirement(name = "BearerAuth"))
public class SpringDocConfig {

    @Bean
    public OpenAPI factoryLinkApi() {
        return new OpenAPI()
            .info(new Info().title("Factorylink后台管理系统")
                .description("Factorylink API 演示")
                .version("v1.8.0")
                .license(new License().name("MIT 3.0").url("https://github.com/valarchie/FactoryLink-Back-End")))
            .externalDocs(new ExternalDocumentation()
                .description("Factorylink后台管理系统接口文档")
                .url("https://juejin.cn/column/7159946528827080734"));
    }

}
