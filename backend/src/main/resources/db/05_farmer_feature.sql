-- ============================================
-- 助农商城 - 农户功能模块数据库迁移脚本
-- 版本: v1.0
-- 日期: 2026-04-12
-- 说明: 本脚本为增量迁移，不会影响现有数据
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 扩展现有表
-- ----------------------------

-- 1.1 扩展用户表 (sys_user)
ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `is_farmer` TINYINT NOT NULL DEFAULT 0 
COMMENT '是否为农户: 0-否 1-是';

ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `farmer_apply_status` TINYINT DEFAULT NULL 
COMMENT '农户申请状态: NULL-未申请 0-待审核 1-已通过 2-已拒绝';

ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `real_name` VARCHAR(50) DEFAULT NULL 
COMMENT '真实姓名';

ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `id_card` VARCHAR(18) DEFAULT NULL 
COMMENT '身份证号';

-- 1.2 扩展商品表 (product)
ALTER TABLE `product` ADD COLUMN IF NOT EXISTS `farmer_id` BIGINT DEFAULT NULL 
COMMENT '发布农户用户ID（NULL表示自营商品）';

ALTER TABLE `product` ADD COLUMN IF NOT EXISTS `product_type` TINYINT NOT NULL DEFAULT 0 
COMMENT '商品类型: 0-自营商品 1-农户商品';

ALTER TABLE `product` ADD COLUMN IF NOT EXISTS `audit_status` TINYINT NOT NULL DEFAULT 1 
COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝（自营默认通过）';

ALTER TABLE `product` ADD COLUMN IF NOT EXISTS `reject_reason` VARCHAR(500) DEFAULT NULL 
COMMENT '拒绝原因';

CREATE INDEX IF NOT EXISTS `idx_product_farmer_id` ON `product`(`farmer_id`);
CREATE INDEX IF NOT EXISTS `idx_product_type` ON `product`(`product_type`);
CREATE INDEX IF NOT EXISTS `idx_product_audit_status` ON `product`(`audit_status`);

-- ----------------------------
-- 2. 创建新表
-- ----------------------------

-- 2.1 农户资料表
CREATE TABLE IF NOT EXISTS `farmer_profile` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
    `shop_name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
    `shop_logo` VARCHAR(500) DEFAULT NULL COMMENT '店铺Logo URL',
    `description` TEXT DEFAULT NULL COMMENT '店铺简介',
    `business_license` VARCHAR(500) DEFAULT NULL COMMENT '营业执照图片URL',
    `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `contact_address` VARCHAR(255) DEFAULT NULL COMMENT '联系地址',
    `province` VARCHAR(50) DEFAULT NULL COMMENT '所在省份',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '所在城市',
    `district` VARCHAR(50) DEFAULT NULL COMMENT '所在区县',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态: 0-待审核 1-已通过 2-已拒绝',
    `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    `auditor_id` BIGINT DEFAULT NULL COMMENT '审核管理员ID',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_auditor_id` (`auditor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农户资料表';

-- 2.2 商品审核记录表
CREATE TABLE IF NOT EXISTS `product_audit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `farmer_id` BIGINT NOT NULL COMMENT '农户用户ID',
    `auditor_id` BIGINT DEFAULT NULL COMMENT '审核管理员ID',
    `audit_status` TINYINT NOT NULL COMMENT '审核状态: 0-待审核 1-通过 2-拒绝',
    `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_farmer_id` (`farmer_id`),
    KEY `idx_auditor_id` (`auditor_id`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品审核记录表';

-- 2.3 店铺表
CREATE TABLE IF NOT EXISTS `shop` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `farmer_id` BIGINT NOT NULL COMMENT '农户用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
    `logo` VARCHAR(500) DEFAULT NULL COMMENT '店铺Logo URL',
    `banner_image` VARCHAR(500) DEFAULT NULL COMMENT '店铺横幅图片URL',
    `description` TEXT DEFAULT NULL COMMENT '店铺简介',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `contact_wechat` VARCHAR(100) DEFAULT NULL COMMENT '微信号',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '店铺地址',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '访问次数',
    `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数量',
    `product_count` INT NOT NULL DEFAULT 0 COMMENT '商品数量',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-关闭 1-营业中',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_farmer_id` (`farmer_id`),
    KEY `idx_status` (`status`),
    KEY `idx_view_count` (`view_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺表';

-- 2.4 消息表
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型: 1-系统通知 2-审核通知 3-订单通知',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联业务ID（如商品ID、订单ID）',
    `related_type` VARCHAR(50) DEFAULT NULL COMMENT '关联业务类型',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 2.5 商品评价表
CREATE TABLE IF NOT EXISTS `product_review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `user_id` BIGINT NOT NULL COMMENT '评价用户ID（买家）',
    `farmer_id` BIGINT NOT NULL COMMENT '农户用户ID（卖家）',
    `rating` TINYINT NOT NULL COMMENT '评分: 1-5星',
    `content` TEXT DEFAULT NULL COMMENT '评价内容',
    `images` JSON DEFAULT NULL COMMENT '评价图片列表(JSON数组)',
    `reply_content` TEXT DEFAULT NULL COMMENT '农户回复内容',
    `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-隐藏 1-显示',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_farmer_id` (`farmer_id`),
    KEY `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';

-- 2.6 商品举报表
CREATE TABLE IF NOT EXISTS `product_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT NOT NULL COMMENT '被举报商品ID',
    `reporter_id` BIGINT NOT NULL COMMENT '举报人用户ID',
    `reason_type` TINYINT NOT NULL COMMENT '举报原因类型: 1-假冒伪劣 2-价格欺诈 3-虚假宣传 4-其他',
    `reason_detail` TEXT NOT NULL COMMENT '举报详细说明',
    `evidence_images` JSON DEFAULT NULL COMMENT '证据图片(JSON数组)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态: 0-待处理 1-已核实 2-已忽略 3-已处罚',
    `handler_id` BIGINT DEFAULT NULL COMMENT '处理人管理员ID',
    `handle_result` VARCHAR(500) DEFAULT NULL COMMENT '处理结果说明',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_status` (`status`),
    KEY `idx_handler_id` (`handler_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品举报表';

-- 2.7 店铺收藏表
CREATE TABLE IF NOT EXISTS `shop_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_shop` (`user_id`, `shop_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺收藏表';

SET FOREIGN_KEY_CHECKS = 1;

SELECT '✅ 农户功能模块数据库迁移完成！新增7张表，扩展2张表' AS message;
