-- ============================================
-- 助农商城数据初始化脚本
-- 数据库: zhunong-shangcheng
-- ============================================

USE `zhunong-shangcheng`;

-- 密码使用BCrypt加密: 123456 -> $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO

-- ============================================
-- 1. 初始化管理员账户 (admin/123456)
-- ============================================
INSERT INTO sys_admin (id, username, password, nickname, email, phone, status, create_time, update_time) VALUES
(100, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '超级管理员', 'admin@zhunong.com', '13800138000', 1, NOW(), NOW());

-- ============================================
-- 2. 初始化商品分类
-- ============================================
INSERT INTO product_category (id, name, parent_id, level, sort_order, icon, status, create_time, update_time) VALUES
-- 一级分类
(100, '新鲜蔬菜', 0, 1, 1, 'fa-carrot', 1, NOW(), NOW()),
(101, '时令水果', 0, 1, 2, 'fa-apple-alt', 1, NOW(), NOW()),
(102, '禽蛋肉类', 0, 1, 3, 'fa-drumstick-bite', 1, NOW(), NOW()),
(103, '粮油米面', 0, 1, 4, 'fa-bread-slice', 1, NOW(), NOW()),
(104, '干货特产', 0, 1, 5, 'fa-seedling', 1, NOW(), NOW()),
(105, '农副加工', 0, 1, 6, 'fa-cheese', 1, NOW(), NOW()),
-- 二级分类-蔬菜
(200, '叶菜类', 100, 2, 1, NULL, 1, NOW(), NOW()),
(201, '根茎类', 100, 2, 2, NULL, 1, NOW(), NOW()),
(202, '茄果类', 100, 2, 3, NULL, 1, NOW(), NOW()),
-- 二级分类-水果
(203, '苹果梨桃', 101, 2, 1, NULL, 1, NOW(), NOW()),
(204, '柑橘橙柚', 101, 2, 2, NULL, 1, NOW(), NOW()),
(205, '热带水果', 101, 2, 3, NULL, 1, NOW(), NOW()),
-- 二级分类-禽蛋肉类
(206, '猪肉', 102, 2, 1, NULL, 1, NOW(), NOW()),
(207, '牛羊肉', 102, 2, 2, NULL, 1, NOW(), NOW()),
(208, '禽类', 102, 2, 3, NULL, 1, NOW(), NOW()),
(209, '蛋类', 102, 2, 4, NULL, 1, NOW(), NOW());

-- ============================================
-- 3. 初始化商品数据
-- ============================================
INSERT INTO product (id, name, category_id, description, detail, price, original_price, stock, sales, main_image, images, origin, unit, weight, status, is_hot, is_new, is_help, sort_order, create_time, update_time) VALUES
-- 蔬菜类
(1000, '有机蔬菜礼盒 新鲜时令蔬菜 5斤装', 100, '源自生态农场，直供新鲜蔬果', '<p>有机蔬菜礼盒，包含多种时令蔬菜，新鲜采摘，当天发货。</p><p>包含：菠菜、生菜、小白菜、油麦菜等5-6种时令蔬菜。</p>', 59.90, 79.90, 100, 128, 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600', '["https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600","https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600"]', '山东·寿光', '盒', 2.5, 1, 1, 0, 0, 1, NOW(), NOW()),
(1001, '有机菠菜 500g 新鲜采摘', 200, '农家自种，现摘现发', '<p>有机种植菠菜，无农药残留，新鲜健康。</p>', 16.80, 22.00, 80, 78, 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600', '["https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600"]', '云南·元谋', '把', 0.5, 1, 0, 0, 0, 10, NOW(), NOW()),
(1002, '新鲜胡萝卜 1kg 农家种植', 201, '口感脆甜，营养丰富', '<p>农家自种胡萝卜，新鲜出土，口感脆甜。</p>', 12.90, 16.00, 150, 56, 'https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=600', '["https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=600"]', '河北·保定', '袋', 1.0, 1, 0, 1, 0, 11, NOW(), NOW()),
(1003, '新鲜西红柿 1kg 自然成熟', 202, '沙瓤多汁，酸甜可口', '<p>自然成熟西红柿，沙瓤多汁，适合生吃或做菜。</p>', 15.80, 20.00, 120, 89, 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=600', '["https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=600"]', '山东·临沂', '盒', 1.0, 1, 0, 0, 0, 12, NOW(), NOW()),

-- 水果类
(1004, '陕西红富士苹果 8斤装 脆甜多汁', 203, '陕西特产，脆甜多汁，果径80-85mm', '<p>正宗陕西红富士苹果，阳光充足，口感脆甜。</p>', 79.00, 99.00, 200, 256, 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600', '["https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600"]', '陕西·延安', '箱', 4.0, 1, 1, 0, 1, 2, NOW(), NOW()),
(1005, '赣南脐橙 5kg 精品果', 204, '赣南特产，汁多味甜', '<p>正宗赣南脐橙，果大形正，橙红鲜艳，汁多味甜。</p>', 56.80, 78.00, 150, 96, 'https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?w=600', '["https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?w=600"]', '江西·赣州', '箱', 5.0, 1, 0, 1, 1, 9, NOW(), NOW()),
(1006, '海南香蕉 5斤装 新鲜直达', 205, '海南特产，软糯香甜', '<p>海南香蕉，自然成熟，软糯香甜。</p>', 29.90, 39.00, 180, 132, 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=600', '["https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=600"]', '海南·三亚', '箱', 2.5, 1, 1, 0, 0, 12, NOW(), NOW()),
(1007, '奶油草莓 2斤装 新鲜现摘', 205, '现摘现发，甜而不腻', '<p>新鲜奶油草莓，个大饱满，甜度适中。</p>', 68.00, 88.00, 120, 87, 'https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=600', '["https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=600"]', '江苏·南京', '盒', 1.0, 1, 0, 0, 0, 6, NOW(), NOW()),

-- 粮油米面
(1008, '五常大米 5kg 东北大米 优质粳米', 103, '东北五常大米，稻花香2号', '<p>正宗五常大米，产自黑龙江五常市，米粒饱满，香味浓郁。</p>', 68.50, 88.00, 150, 98, 'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600', '["https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600"]', '黑龙江·五常', '袋', 5.0, 1, 0, 0, 0, 3, NOW(), NOW()),
(1009, '有机大豆油 5L 非转基因', 103, '非转基因大豆，物理压榨', '<p>选用优质非转基因大豆，物理压榨工艺，保留营养。</p>', 79.90, 99.00, 100, 103, 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600', '["https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600"]', '山东·青岛', '桶', 5.0, 1, 0, 1, 0, 7, NOW(), NOW()),
(1010, '全麦面粉 5kg 石磨工艺', 103, '石磨研磨，保留麦香', '<p>传统石磨工艺，低温研磨，保留小麦原有营养。</p>', 45.00, 58.00, 80, 45, 'https://images.unsplash.com/photo-1627485937980-221c88ac04f9?w=600', '["https://images.unsplash.com/photo-1627485937980-221c88ac04f9?w=600"]', '河南·新乡', '袋', 5.0, 1, 0, 0, 0, 8, NOW(), NOW()),

-- 禽蛋肉类
(1011, '农家散养土鸡蛋 30枚装 新鲜营养', 209, '散养土鸡蛋，新鲜直达', '<p>农家散养土鸡，自由觅食，鸡蛋营养丰富。</p>', 45.90, 55.00, 300, 324, 'https://images.unsplash.com/photo-1506976785307-8732e854ad03?w=600', '["https://images.unsplash.com/photo-1506976785307-8732e854ad03?w=600"]', '安徽·黄山', '盒', 1.5, 1, 1, 0, 0, 4, NOW(), NOW()),
(1012, '土猪肉五花肉 500g 农家散养', 206, '肥瘦相间，肉质鲜嫩', '<p>农家散养土猪，肉质鲜嫩，肥而不腻。</p>', 38.90, 48.00, 60, 67, 'https://images.unsplash.com/photo-1607623814075-e51df1bd6565?w=600', '["https://images.unsplash.com/photo-1607623814075-e51df1bd6565?w=600"]', '四川·成都', '袋', 0.5, 1, 0, 0, 0, 13, NOW(), NOW()),
(1013, '草原牛肉 1kg 新鲜冷冻', 207, '草原放养，肉质紧实', '<p>内蒙古草原牛肉，肉质紧实，口感鲜美。</p>', 89.00, 108.00, 40, 34, 'https://images.unsplash.com/photo-1603048297172-c92544798d52?w=600', '["https://images.unsplash.com/photo-1603048297172-c92544798d52?w=600"]', '内蒙古·呼伦贝尔', '袋', 1.0, 1, 0, 1, 0, 14, NOW(), NOW()),
(1014, '散养土鸡 整只 1.5kg', 208, '农家散养，肉质紧实', '<p>农家散养土鸡，肉质紧实，煲汤佳品。</p>', 128.00, 158.00, 30, 23, 'https://images.unsplash.com/photo-1548550023-2bdb3c5b3d0e?w=600', '["https://images.unsplash.com/photo-1548550023-2bdb3c5b3d0e?w=600"]', '广东·清远', '只', 1.5, 1, 0, 0, 0, 15, NOW(), NOW()),

-- 干货特产
(1015, '纯天然野花蜂蜜 500g 野生蜂蜜', 104, '纯天然野生蜂蜜，无添加', '<p>采自深山野花，纯天然无添加，口感醇厚。</p>', 89.00, 128.00, 80, 156, 'https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600', '["https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600"]', '云南·昆明', '瓶', 0.5, 1, 0, 1, 0, 5, NOW(), NOW()),
(1016, '新疆薄皮核桃 500g 原味', 104, '新疆特产，原味无添加', '<p>新疆阿克苏薄皮核桃，壳薄仁满，原味香脆。</p>', 39.90, 55.00, 200, 64, 'https://images.unsplash.com/photo-1575481636764-7f2365801449?w=600', '["https://images.unsplash.com/photo-1575481636764-7f2365801449?w=600"]', '新疆·阿克苏', '袋', 0.5, 1, 0, 0, 0, 8, NOW(), NOW()),
(1017, '东北黑木耳 250g 干货', 104, '东北特产，肉厚口感好', '<p>东北黑木耳，朵大肉厚，口感爽脆。</p>', 35.80, 48.00, 120, 45, 'https://images.unsplash.com/photo-1594035900144-17e9e019e8f8?w=600', '["https://images.unsplash.com/photo-1594035900144-17e9e019e8f8?w=600"]', '黑龙江·牡丹江', '袋', 0.25, 1, 0, 0, 0, 11, NOW(), NOW()),
(1018, '宁夏枸杞 500g 特级', 104, '宁夏特产，滋补佳品', '<p>宁夏中宁枸杞，颗粒饱满，色泽红润。</p>', 68.00, 88.00, 90, 78, 'https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=600', '["https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=600"]', '宁夏·中宁', '袋', 0.5, 1, 0, 1, 0, 16, NOW(), NOW()),

-- 农副加工
(1019, '手工红薯粉条 500g 农家自制', 105, '传统工艺，口感筋道', '<p>农家自制红薯粉条，传统工艺，口感筋道爽滑。</p>', 25.80, 32.00, 100, 89, 'https://images.unsplash.com/photo-1612929633738-8fe44f7ec841?w=600', '["https://images.unsplash.com/photo-1612929633738-8fe44f7ec841?w=600"]', '河南·周口', '袋', 0.5, 1, 0, 0, 0, 17, NOW(), NOW()),
(1020, '农家自制腊肉 500g 烟熏风味', 105, '传统工艺，烟熏风味', '<p>农家自制腊肉，传统工艺腌制，烟熏风味浓郁。</p>', 58.00, 72.00, 50, 112, 'https://images.unsplash.com/photo-1603048297172-c92544798d52?w=600', '["https://images.unsplash.com/photo-1603048297172-c92544798d52?w=600"]', '湖南·湘西', '袋', 0.5, 1, 1, 0, 0, 18, NOW(), NOW());

-- ============================================
-- 4. 初始化轮播图
-- ============================================
INSERT INTO carousel (id, title, subtitle, image, link, sort_order, status, create_time, update_time) VALUES
(100, '新鲜直达 品质保证', '精选优质农产品，从田间到餐桌', 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=1920&h=400&fit=crop', '/products', 1, 1, NOW(), NOW()),
(101, '助农惠农 乡村振兴', '支持农民增收，助力乡村发展', 'https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=1920&h=400&fit=crop', '/help', 2, 1, NOW(), NOW()),
(102, '绿色有机 健康生活', '天然无污染，吃得更放心', 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=1920&h=400&fit=crop', '/products', 3, 1, NOW(), NOW()),
(103, '时令水果 新鲜上市', '应季水果，新鲜采摘', 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=1920&h=400&fit=crop', '/category', 4, 1, NOW(), NOW());

-- ============================================
-- 5. 初始化助农活动
-- ============================================
INSERT INTO activity (id, title, subtitle, description, image, banner_image, link, start_time, end_time, sort_order, status, create_time, update_time) VALUES
(100, '助农专区', '支持乡村振兴，购买直采农产品', '<p>助农专区汇集了来自全国各地的优质农产品，直接对接农户，减少中间环节，让消费者买到物美价廉的农产品，同时帮助农民增收致富。</p>', 'https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=800', 'https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=1920', '/help', NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 1, 1, NOW(), NOW()),
(101, '春季新鲜季', '春季时令蔬菜水果特惠', '<p>春季新鲜蔬菜水果上市，新鲜直达，优惠多多。</p>', 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=800', 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=1920', '/products', NOW(), DATE_ADD(NOW(), INTERVAL 3 MONTH), 2, 1, NOW(), NOW()),
(102, '丰收节特惠', '庆祝丰收，全场特惠', '<p>丰收节期间，全场农产品特惠，助农惠农。</p>', 'https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=800', 'https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=1920', '/sale', NOW(), DATE_ADD(NOW(), INTERVAL 6 MONTH), 3, 1, NOW(), NOW());

-- 输出完成信息
SELECT '数据初始化完成！' AS message;
SELECT CONCAT('管理员: ', (SELECT COUNT(*) FROM sys_admin), ' 个') AS admin_count;
SELECT CONCAT('商品分类: ', (SELECT COUNT(*) FROM product_category), ' 个') AS category_count;
SELECT CONCAT('商品: ', (SELECT COUNT(*) FROM product), ' 个') AS product_count;
SELECT CONCAT('轮播图: ', (SELECT COUNT(*) FROM carousel), ' 个') AS carousel_count;
SELECT CONCAT('活动: ', (SELECT COUNT(*) FROM activity), ' 个') AS activity_count;
