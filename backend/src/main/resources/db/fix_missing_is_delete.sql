-- 为所有缺少is_delete字段的表添加is_delete字段
-- 修复日期: 2026-04-14

USE `zhunong-shangcheng`;

-- 1. 为product表添加is_delete字段（如果不存在）
ALTER TABLE `product` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 2. 为shopping_cart表添加is_delete字段（如果不存在）
ALTER TABLE `shopping_cart` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 3. 为category表添加is_delete字段（如果不存在）
ALTER TABLE `category` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 4. 为sys_user表添加is_delete字段（如果不存在）
ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 5. 为banner表添加is_delete字段（如果不存在）
ALTER TABLE `banner` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 6. 为order_info表添加is_delete字段（如果不存在）
ALTER TABLE `order_info` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 7. 为order_item表添加is_delete字段（如果不存在）
ALTER TABLE `order_item` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 8. 为address表添加is_delete字段（如果不存在）
ALTER TABLE `address` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 9. 为farmer_profile表添加is_delete字段（如果不存在）
ALTER TABLE `farmer_profile` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 10. 为product_audit表添加is_delete字段（如果不存在）
ALTER TABLE `product_audit` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 11. 为product_report表添加is_delete字段（如果不存在）
ALTER TABLE `product_report` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 12. 为product_review表添加is_delete字段（如果不存在）
ALTER TABLE `product_review` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 13. 为shop表添加is_delete字段（如果不存在）
ALTER TABLE `shop` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 14. 为shop_favorite表添加is_delete字段（如果不存在）
ALTER TABLE `shop_favorite` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 15. 为special_product表添加is_delete字段（如果不存在）
ALTER TABLE `special_product` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 16. 为favorite表添加is_delete字段（如果不存在）
ALTER TABLE `favorite` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 17. 为message表添加is_delete字段（如果不存在）
ALTER TABLE `message` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 18. 为activity表添加is_delete字段（如果不存在）
ALTER TABLE `activity` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 19. 为product_detail表添加is_delete字段（如果不存在）
ALTER TABLE `product_detail` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

-- 20. 为admin表添加is_delete字段（如果不存在）
ALTER TABLE `admin` ADD COLUMN IF NOT EXISTS `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`;

SELECT '所有表的is_delete字段已添加完成！' AS message;
