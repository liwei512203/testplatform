package com.example.testplatform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档配置（OpenAPI3 / Swagger）：
 * - Knife4j 增强文档页：http://localhost:8082/doc.html
 * - 原生 Swagger UI：http://localhost:8082/swagger-ui/index.html
 * - OpenAPI 描述文件：http://localhost:8082/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("测试管理平台后端接口")
                        .description("测试用例管理平台后端 API：项目、测试用例、版本计划、统计报表等模块接口文档")
                        .version("1.0.0"));
    }

    /**
     * 基础业务分组：项目 / 用例 / 版本计划 / 字典 / 统计管理
     */
    @Bean
    public GroupedOpenApi bizApi() {
        return GroupedOpenApi.builder()
                .group("基础业务模块")
                .packagesToScan("com.example.testplatform.controller")
                .build();
    }

    /**
     * 报表分组：统计报表查询与 Excel 导出
     */
    @Bean
    public GroupedOpenApi reportApi() {
        return GroupedOpenApi.builder()
                .group("统计报表模块")
                .packagesToScan("com.example.testplatform.report.controller")
                .build();
    }
}
