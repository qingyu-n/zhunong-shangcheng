package com.zhunong.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 助农商城管理系统主启动类
 *
 * @author 助农商城开发团队
 * @version 1.0.0
 * @since 2026-03-22
 */
@SpringBootApplication
public class ZhunongMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhunongMallApplication.class, args);
        System.out.println("========================================");
        System.out.println("  助农商城管理系统启动成功！");
        System.out.println("  API文档地址: http://localhost:8080/api/doc.html");
        System.out.println("========================================");
    }
}
