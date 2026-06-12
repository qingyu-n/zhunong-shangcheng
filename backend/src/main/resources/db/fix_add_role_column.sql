-- ============================================
-- 修复脚本：为 sys_user 表添加 role 字段
-- 解决 API 测试中的 "Unknown column 'role' in 'field list'" 错误
-- ============================================

USE `zhunong-shangcheng`;

-- 为 sys_user 表添加 role 字段
ALTER TABLE sys_user 
ADD COLUMN role VARCHAR(20) DEFAULT 'USER' COMMENT '角色: ADMIN-管理员 USER-普通用户' AFTER status;

-- 更新现有数据，设置默认角色为 USER
UPDATE sys_user SET role = 'USER' WHERE role IS NULL;

-- 验证修改
DESCRIBE sys_user;
