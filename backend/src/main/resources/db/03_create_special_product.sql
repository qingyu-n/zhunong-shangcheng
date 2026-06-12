-- 创建限时特惠商品表
CREATE TABLE IF NOT EXISTS `special_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT DEFAULT NULL COMMENT '关联商品ID',
  `type` VARCHAR(50) NOT NULL DEFAULT 'special_offer' COMMENT '类型: special_offer-限时特惠',
  `name` VARCHAR(200) DEFAULT NULL COMMENT '特惠名称',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `discount_price` DECIMAL(10,2) DEFAULT NULL COMMENT '特惠价',
  `discount_percent` INT DEFAULT NULL COMMENT '折扣率（如85表示8.5折）',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用，2-已结束',
  `sort_order` INT DEFAULT 0 COMMENT '排序值',
  `description` TEXT COMMENT '活动描述',
  `image` VARCHAR(500) DEFAULT NULL COMMENT '展示图片',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  INDEX `idx_type` (`type`),
  INDEX `idx_product_id` (`product_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='特殊商品管理表';

-- 确保product表有is_hot和is_help字段
ALTER TABLE `product` ADD COLUMN IF NOT EXISTS `is_hot` TINYINT DEFAULT 0 COMMENT '是否热销：0-否，1-是';
ALTER TABLE `product` ADD COLUMN IF NOT EXISTS `is_help` TINYINT DEFAULT 0 COMMENT '是否助农优选：0-否，1-是';
