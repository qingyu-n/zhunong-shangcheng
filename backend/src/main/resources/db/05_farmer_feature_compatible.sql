-- ============================================
-- 助农商城 - 农户功能模块数据库迁移脚本 (兼容版)
-- 版本: v1.1
-- 说明: 兼容所有MySQL 8.0+版本
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 扩展现有表
-- ----------------------------

-- 1.1 扩展用户表 (sys_user)
SET @dbname = DATABASE();
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = @dbname 
AND TABLE_NAME = 'sys_user' 
AND COLUMN_NAME = 'is_farmer';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `sys_user` ADD COLUMN `is_farmer` TINYINT NOT NULL DEFAULT 0', 
    'SELECT "Column is_farmer already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = @dbname 
AND TABLE_NAME = 'sys_user' 
AND COLUMN_NAME = 'farmer_apply_status';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `sys_user` ADD COLUMN `farmer_apply_status` TINYINT DEFAULT NULL', 
    'SELECT "Column farmer_apply_status already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = @dbname 
AND TABLE_NAME = 'sys_user' 
AND COLUMN_NAME = 'real_name';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `sys_user` ADD COLUMN `real_name` VARCHAR(50) DEFAULT NULL', 
    'SELECT "Column real_name already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = @dbname 
AND TABLE_NAME = 'sys_user' 
AND COLUMN_NAME = 'id_card';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `sys_user` ADD COLUMN `id_card` VARCHAR(18) DEFAULT NULL', 
    'SELECT "Column id_card already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.2 扩展商品表 (product)
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = @dbname 
AND TABLE_NAME = 'product' 
AND COLUMN_NAME = 'farmer_id';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `product` ADD COLUMN `farmer_id` BIGINT DEFAULT NULL', 
    'SELECT "Column farmer_id already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = @dbname 
AND TABLE_NAME = 'product' 
AND COLUMN_NAME = 'product_type';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `product` ADD COLUMN `product_type` TINYINT NOT NULL DEFAULT 0', 
    'SELECT "Column product_type already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = @dbname 
AND TABLE_NAME = 'product' 
AND COLUMN_NAME = 'audit_status';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `product` ADD COLUMN `audit_status` TINYINT NOT NULL DEFAULT 1', 
    'SELECT "Column audit_status already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = @dbname 
AND TABLE_NAME = 'product' 
AND COLUMN_NAME = 'reject_reason';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `product` ADD COLUMN `reject_reason` VARCHAR(500) DEFAULT NULL', 
    'SELECT "Column reject_reason already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 创建索引（如果不存在）
SELECT COUNT(*) INTO @idx_exists 
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = @dbname 
AND TABLE_NAME = 'product' 
AND INDEX_NAME = 'idx_product_farmer_id';

SET @sql = IF(@idx_exists = 0, 
    'CREATE INDEX idx_product_farmer_id ON product(farmer_id)', 
    'SELECT "Index idx_product_farmer_id already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @idx_exists 
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = @dbname 
AND TABLE_NAME = 'product' 
AND INDEX_NAME = 'idx_product_type';

SET @sql = IF(@idx_exists = 0, 
    'CREATE INDEX idx_product_type ON product(product_type)', 
    'SELECT "Index idx_product_type already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @idx_exists 
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = @dbname 
AND TABLE_NAME = 'product' 
AND INDEX_NAME = 'idx_product_audit_status';

SET @sql = IF(@idx_exists = 0, 
    'CREATE INDEX idx_product_audit_status ON product(audit_status)', 
    'SELECT "Index idx_product_audit_status already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 2. 创建新表
-- ----------------------------

-- 2.1 农户资料表
CREATE TABLE IF NOT EXISTS `farmer_profile` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `shop_name` VARCHAR(100) NOT NULL,
    `shop_logo` VARCHAR(500) DEFAULT NULL,
    `description` TEXT DEFAULT NULL,
    `business_license` VARCHAR(500) DEFAULT NULL,
    `contact_name` VARCHAR(50) DEFAULT NULL,
    `contact_phone` VARCHAR(20) DEFAULT NULL,
    `contact_address` VARCHAR(255) DEFAULT NULL,
    `province` VARCHAR(50) DEFAULT NULL,
    `city` VARCHAR(50) DEFAULT NULL,
    `district` VARCHAR(50) DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 0,
    `audit_remark` VARCHAR(500) DEFAULT NULL,
    `auditor_id` BIGINT DEFAULT NULL,
    `audit_time` DATETIME DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_delete` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_auditor_id` (`auditor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.2 商品审核记录表
CREATE TABLE IF NOT EXISTS `product_audit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL,
    `farmer_id` BIGINT NOT NULL,
    `auditor_id` BIGINT DEFAULT NULL,
    `audit_status` TINYINT NOT NULL,
    `audit_remark` VARCHAR(500) DEFAULT NULL,
    `audit_time` DATETIME DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_farmer_id` (`farmer_id`),
    KEY `idx_auditor_id` (`auditor_id`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.3 店铺表
CREATE TABLE IF NOT EXISTS `shop` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `farmer_id` BIGINT NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `logo` VARCHAR(500) DEFAULT NULL,
    `banner_image` VARCHAR(500) DEFAULT NULL,
    `description` TEXT DEFAULT NULL,
    `contact_phone` VARCHAR(20) DEFAULT NULL,
    `contact_wechat` VARCHAR(100) DEFAULT NULL,
    `address` VARCHAR(255) DEFAULT NULL,
    `view_count` INT NOT NULL DEFAULT 0,
    `favorite_count` INT NOT NULL DEFAULT 0,
    `product_count` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_delete` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_farmer_id` (`farmer_id`),
    KEY `idx_status` (`status`),
    KEY `idx_view_count` (`view_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.4 消息表
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `type` TINYINT NOT NULL DEFAULT 1,
    `related_id` BIGINT DEFAULT NULL,
    `related_type` VARCHAR(50) DEFAULT NULL,
    `is_read` TINYINT NOT NULL DEFAULT 0,
    `read_time` DATETIME DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.5 商品评价表
CREATE TABLE IF NOT EXISTS `product_review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL,
    `order_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `farmer_id` BIGINT NOT NULL,
    `rating` TINYINT NOT NULL,
    `content` TEXT DEFAULT NULL,
    `images` JSON DEFAULT NULL,
    `reply_content` TEXT DEFAULT NULL,
    `reply_time` DATETIME DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_delete` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_farmer_id` (`farmer_id`),
    KEY `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.6 商品举报表
CREATE TABLE IF NOT EXISTS `product_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL,
    `reporter_id` BIGINT NOT NULL,
    `reason_type` TINYINT NOT NULL,
    `reason_detail` TEXT NOT NULL,
    `evidence_images` JSON DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 0,
    `handler_id` BIGINT DEFAULT NULL,
    `handle_result` VARCHAR(500) DEFAULT NULL,
    `handle_time` DATETIME DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_status` (`status`),
    KEY `idx_handler_id` (`handler_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.7 店铺收藏表
CREATE TABLE IF NOT EXISTS `shop_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `shop_id` BIGINT NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_shop` (`user_id`, `shop_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Migration completed successfully!' AS message;
