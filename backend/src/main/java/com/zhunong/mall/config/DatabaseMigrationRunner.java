package com.zhunong.mall.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        addColumnIfNotExists("order_info", "logistics_company", "VARCHAR(100) DEFAULT NULL");
        addColumnIfNotExists("order_info", "tracking_number", "VARCHAR(100) DEFAULT NULL");
        log.info("数据库迁移完成");
    }

    private void addColumnIfNotExists(String tableName, String columnName, String columnDefinition) {
        try {
            String checkSql = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, tableName, columnName);
            if (count != null && count == 0) {
                String alterSql = String.format("ALTER TABLE `%s` ADD COLUMN `%s` %s", tableName, columnName, columnDefinition);
                jdbcTemplate.execute(alterSql);
                log.info("已添加字段: {}.{}", tableName, columnName);
            }
        } catch (Exception e) {
            log.warn("添加字段 {}.{} 时出错: {}", tableName, columnName, e.getMessage());
        }
    }
}
