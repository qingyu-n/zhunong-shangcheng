-- ============================================
-- 助农商城 - 农户功能模块测试数据初始化脚本
-- 执行时间: 2026-04-12
-- 用途: 创建测试账号、预置数据
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `zhunong-shangcheng`;

-- ----------------------------
-- 1. 清理历史测试数据（按依赖顺序）
-- ----------------------------
DELETE FROM `shop_favorite` WHERE 1=1;
DELETE FROM `product_report` WHERE 1=1;
DELETE FROM `product_review` WHERE 1=1;
DELETE FROM `message` WHERE 1=1;
DELETE FROM `product_audit` WHERE 1=1;
DELETE FROM `shop` WHERE 1=1;
DELETE FROM `farmer_profile` WHERE 1=1;

-- ----------------------------
-- 2. 重置用户表中的农户相关字段
-- ----------------------------
UPDATE `sys_user` SET 
  `is_farmer` = 0, 
  `farmer_apply_status` = NULL, 
  `real_name` = NULL,
  `id_card` = NULL
WHERE `username` IN ('user_farm', 'farmer01', 'farmer02');

-- 重置商品表中的农户相关字段
UPDATE `product` SET 
  `farmer_id` = NULL, 
  `product_type` = 0, 
  `audit_status` = 1,
  `reject_reason` = NULL
WHERE 1=1;

-- ----------------------------
-- 3. 插入/更新测试用户
-- ----------------------------
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `phone`, `role`, `status`, `is_farmer`) VALUES
(1001, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', '13800000000', 'ADMIN', 1, 0),
(2001, 'buyer01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '普通买家', '13900000001', 'USER', 1, 0),
(3001, 'user_farm', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '准农户', '13900000002', 'USER', 1, 0)
ON DUPLICATE KEY UPDATE
  `nickname` = VALUES(`nickname`),
  `phone` = VALUES(`phone`),
  `role` = VALUES(`role`),
  `status` = VALUES(`status`),
  `is_farmer` = VALUES(`is_farmer`);

INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `phone`, `role`, `status`, `is_farmer`, `farmer_apply_status`, `real_name`) VALUES
(4001, 'farmer01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张三农场', '13800000001', 'USER', 1, 1, 1, '张三'),
(5001, 'farmer02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李四果园', '13800000002', 'USER', 1, 0, 2, '李四')
ON DUPLICATE KEY UPDATE
  `nickname` = VALUES(`nickname`),
  `phone` = VALUES(`phone`),
  `role` = VALUES(`role`),
  `status` = VALUES(`status`),
  `is_farmer` = VALUES(`is_farmer`),
  `farmer_apply_status` = VALUES(`farmer_apply_status`),
  `real_name` = VALUES(`real_name`);

-- ----------------------------
-- 4. 为farmer01预置农户资料（已通过状态）
-- ----------------------------
INSERT INTO `farmer_profile` (`user_id`, `shop_name`, `description`, `contact_name`, `contact_phone`, `province`, `city`, `district`, `status`, `auditor_id`, `audit_time`) VALUES
(4001, '张三的农家小院', '专注有机蔬菜种植10年，坚持绿色种植理念，为您提供最新鲜的农产品', '张三', '13800000001', '山东省', '潍坊市', '寿光市', 1, 1001, NOW())
ON DUPLICATE KEY UPDATE
  `shop_name` = VALUES(`shop_name`),
  `description` = VALUES(`description`),
  `status` = VALUES(`status`);

-- ----------------------------
-- 5. 为farmer02预置农户资料（已拒绝状态）
-- ----------------------------
INSERT INTO `farmer_profile` (`user_id`, `shop_name`, `description`, `contact_name`, `contact_phone`, `province`, `city`, `district`, `status`, `audit_remark`, `auditor_id`, `audit_time`) VALUES
(5001, '李四的生态果园', '绿色水果，自然味道', '李四', '13800000002', '陕西省', '西安市', '周至县', 2, '营业执照不清晰，请重新上传清晰的证件照片', 1001, NOW())
ON DUPLICATE KEY UPDATE
  `shop_name` = VALUES(`shop_name`),
  `audit_remark` = VALUES(`audit_remark`),
  `status` = VALUES(`status`);

-- ----------------------------
-- 6. 插入测试店铺（为farmer01创建店铺）
-- ----------------------------
INSERT INTO `shop` (`farmer_id`, `name`, `logo`, `banner_image`, `description`, `contact_phone`, `address`, `view_count`, `favorite_count`, `product_count`, `status`) VALUES
(4001, '张三的农家小院', '', '', '专注有机蔬菜种植10年，坚持绿色种植理念，为您提供最新鲜的农产品。所有产品均为当日采摘，保证新鲜！', '138****8001', '山东省潍坊市寿光市', 1258, 86, 25, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `description` = VALUES(`description`);

-- ----------------------------
-- 7. 验证数据插入结果
-- ----------------------------
SELECT '=== 测试用户 ===' AS section;
SELECT id, username, nickname, role, is_farmer, farmer_apply_status, real_name FROM sys_user WHERE id IN (1001, 2001, 3001, 4001, 5001);

SELECT '=== 农户资料 ===' AS section;
SELECT id, user_id, shop_name, status, audit_remark FROM farmer_profile;

SELECT '=== 店铺信息 ===' AS section;
SELECT id, farmer_id, name, view_count, favorite_count, product_count FROM shop;

SELECT '✅ 测试数据初始化完成!' AS message;

SET FOREIGN_KEY_CHECKS = 1;
