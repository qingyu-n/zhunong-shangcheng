package com.zhunong.mall.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * 数据源配置验证
 * 启动时验证数据库连接
 */
@Component
public class DataSourceConfig implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    private final DataSource dataSource;

    public DataSourceConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            log.info("========================================");
            log.info("数据库连接成功！");
            log.info("数据库URL: {}", metaData.getURL());
            log.info("数据库类型: {}", metaData.getDatabaseProductName());
            log.info("数据库版本: {}", metaData.getDatabaseProductVersion());
            log.info("========================================");
        } catch (SQLException e) {
            log.error("数据库连接失败！", e);
            throw new RuntimeException("无法连接到MySQL数据库，请检查配置", e);
        }
    }
}
