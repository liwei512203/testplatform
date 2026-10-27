package com.example.testplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 测试用例管理平台启动类
 */
@MapperScan({"com.example.testplatform.mapper", "com.example.testplatform.report.mapper"})
@EnableScheduling
@SpringBootApplication
public class TestPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestPlatformApplication.class, args);
    }
}
