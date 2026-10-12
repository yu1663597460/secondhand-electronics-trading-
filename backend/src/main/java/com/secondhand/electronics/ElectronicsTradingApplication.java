package com.secondhand.electronics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ElectronicsTradingApplication {
    
    public static void main(String[] args) {
        try {
            System.out.println("========================================");
            System.out.println("  二手电子产品交易系统后端启动中...");
            System.out.println("========================================");
            
            SpringApplication app = new SpringApplication(ElectronicsTradingApplication.class);
            
            // 设置默认配置
            System.setProperty("spring.jpa.hibernate.ddl-auto", "update");
            System.setProperty("spring.jpa.show-sql", "false");
            
            app.run(args);
            
            System.out.println("========================================");
            System.out.println("  后端服务启动成功！");
            System.out.println("  API地址: http://localhost:8080/api");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("  后端服务启动失败！");
            System.err.println("  错误信息: " + e.getMessage());
            System.err.println("========================================");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
