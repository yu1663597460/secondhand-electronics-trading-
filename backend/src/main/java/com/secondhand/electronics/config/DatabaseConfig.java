package com.secondhand.electronics.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class DatabaseConfig {

    /**
     * 检查MySQL是否可用
     */
    public static boolean isMySQLAvailable() {
        try {
            String url = "jdbc:mysql://localhost:3306/secondhand_electronics?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
            String username = "root";
            String password = "3334520yangmi";
            
            Connection connection = DriverManager.getConnection(url, username, password);
            connection.close();
            return true;
        } catch (Exception e) {
            System.out.println("MySQL不可用，将使用H2内存数据库: " + e.getMessage());
            return false;
        }
    }

    /**
     * MySQL数据源配置
     */
    @Bean
    @Profile("mysql")
    public DataSource mysqlDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/secondhand_electronics?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai");
        dataSource.setUsername("root");
        dataSource.setPassword("3334520yangmi");
        return dataSource;
    }

    /**
     * H2数据源配置
     */
    @Bean
    @Profile("h2")
    public DataSource h2DataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:secondhand_electronics;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}