SET NAMES utf8mb4;
USE `zhunong-shangcheng`;

DELETE FROM shop_favorite;
DELETE FROM product_report;
DELETE FROM product_review;
DELETE FROM message;
DELETE FROM product_audit;
DELETE FROM shop;
DELETE FROM farmer_profile;

UPDATE sys_user SET is_farmer = 0, farmer_apply_status = NULL, real_name = NULL, id_card = NULL WHERE username IN ('user_farm', 'farmer01', 'farmer02');

UPDATE product SET farmer_id = NULL, product_type = 0, audit_status = 1, reject_reason = NULL WHERE 1=1;

INSERT INTO sys_user (id, username, password, nickname, phone, role, status, is_farmer) VALUES
(1001, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', '13800000000', 'ADMIN', 1, 0),
(2001, 'buyer01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '普通买家', '13900000001', 'USER', 1, 0),
(3001, 'user_farm', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '准农户', '13900000002', 'USER', 1, 0)
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), phone = VALUES(phone), role = VALUES(role), is_farmer = VALUES(is_farmer);

INSERT INTO sys_user (id, username, password, nickname, phone, role, status, is_farmer, farmer_apply_status, real_name) VALUES
(4001, 'farmer01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张三农场', '13800000001', 'USER', 1, 1, 1, '张三'),
(5001, 'farmer02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李四果园', '13800000002', 'USER', 1, 0, 2, '李四')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), is_farmer = VALUES(is_farmer), farmer_apply_status = VALUES(farmer_apply_status), real_name = VALUES(real_name);

INSERT INTO farmer_profile (user_id, shop_name, description, contact_name, contact_phone, province, city, district, status, auditor_id, audit_time) VALUES
(4001, '张三的农家小院', '专注有机蔬菜种植10年，坚持绿色种植理念，为您提供最新鲜的农产品', '张三', '13800000001', '山东省', '潍坊市', '寿光市', 1, 1001, NOW())
ON DUPLICATE KEY UPDATE shop_name = VALUES(shop_name), status = VALUES(status);

INSERT INTO farmer_profile (user_id, shop_name, description, contact_name, contact_phone, province, city, district, status, audit_remark, auditor_id, audit_time) VALUES
(5001, '李四的生态果园', '绿色水果，自然味道', '李四', '13800000002', '陕西省', '西安市', '周至县', 2, '营业执照不清晰，请重新上传清晰的证件照片', 1001, NOW())
ON DUPLICATE KEY UPDATE audit_remark = VALUES(audit_remark), status = VALUES(status);

INSERT INTO shop (farmer_id, name, description, contact_phone, address, view_count, favorite_count, product_count, status) VALUES
(4001, '张三的农家小院', '专注有机蔬菜种植10年，坚持绿色种植理念，为您提供最新鲜的农产品。所有产品均为当日采摘，保证新鲜！', '138****8001', '山东省潍坊市寿光市', 1258, 86, 25, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

SELECT id, username, nickname, role, is_farmer, farmer_apply_status FROM sys_user WHERE id IN (1001, 2001, 3001, 4001, 5001);
SELECT id, user_id, shop_name, status FROM farmer_profile;
SELECT id, farmer_id, name, view_count FROM shop;
SELECT 'Test data initialized successfully!' AS result;
