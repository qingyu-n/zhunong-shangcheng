-- 助农商城数据库表结构

-- 禁用外键检查（允许删除有外键约束的表）
SET FOREIGN_KEY_CHECKS = 0;

-- 清理旧表
DROP TABLE IF EXISTS `reviews`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `order_item`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS `cart`;
DROP TABLE IF EXISTS `product`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `address`;
DROP TABLE IF EXISTS `banner`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `favorites`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `carts`;
DROP TABLE IF EXISTS `addresses`;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `gender` tinyint DEFAULT '0' COMMENT '性别：0-保密，1-男，2-女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `role` varchar(20) DEFAULT 'USER' COMMENT '角色：ADMIN-管理员，USER-普通用户',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 商品分类表
CREATE TABLE IF NOT EXISTS `category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID，0为顶级分类',
  `level` int DEFAULT '1' COMMENT '分类层级',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `icon` varchar(255) DEFAULT NULL COMMENT '图标',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL COMMENT '商品名称',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `main_image` varchar(255) DEFAULT NULL COMMENT '主图',
  `images` json DEFAULT NULL COMMENT '图片列表',
  `price` decimal(10,2) NOT NULL COMMENT '售价',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `stock` int DEFAULT '0' COMMENT '库存',
  `sales` int DEFAULT '0' COMMENT '销量',
  `description` text COMMENT '商品描述',
  `detail` text COMMENT '商品详情',
  `origin` varchar(100) DEFAULT NULL COMMENT '产地',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `weight` decimal(10,2) DEFAULT NULL COMMENT '重量',
  `specifications` json DEFAULT NULL COMMENT '规格参数',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-下架，1-上架',
  `is_hot` tinyint DEFAULT '0' COMMENT '是否热销：0-否，1-是',
  `is_new` tinyint DEFAULT '0' COMMENT '是否新品：0-否，1-是',
  `is_help` tinyint DEFAULT '0' COMMENT '是否助农产品：0-否，1-是',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 购物车表
CREATE TABLE IF NOT EXISTS `cart` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `selected` tinyint DEFAULT '1' COMMENT '是否选中：0-未选中，1-选中',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- 订单表
CREATE TABLE IF NOT EXISTS `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `freight_amount` decimal(10,2) DEFAULT NULL COMMENT '运费金额',
  `discount_amount` decimal(10,2) DEFAULT NULL COMMENT '优惠金额',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `pay_type` tinyint DEFAULT NULL COMMENT '支付方式：1-微信支付，2-支付宝支付',
  `status` tinyint DEFAULT '0' COMMENT '订单状态：0-待付款，1-已付款，2-已发货，3-已完成，4-已取消',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
  `receiver_name` varchar(50) DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收货人电话',
  `receiver_address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `remark` varchar(500) DEFAULT NULL COMMENT '订单备注',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 订单商品表
CREATE TABLE IF NOT EXISTS `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称',
  `product_image` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10,2) NOT NULL COMMENT '单价',
  `quantity` int NOT NULL COMMENT '数量',
  `total_amount` decimal(10,2) NOT NULL COMMENT '小计金额',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品表';

-- 收货地址表
CREATE TABLE IF NOT EXISTS `address` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `province` varchar(50) NOT NULL COMMENT '省份',
  `city` varchar(50) NOT NULL COMMENT '城市',
  `district` varchar(50) NOT NULL COMMENT '区县',
  `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认：0-否，1-是',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- 轮播图表
CREATE TABLE IF NOT EXISTS `banner` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) DEFAULT NULL COMMENT '标题',
  `image` varchar(255) NOT NULL COMMENT '图片地址',
  `link` varchar(255) DEFAULT NULL COMMENT '链接地址',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

-- 插入默认管理员账号（密码：admin123）
INSERT IGNORE INTO `sys_user` (`username`, `password`, `nickname`, `email`, `phone`, `role`, `status`) VALUES
('admin', '$2a$10$kaHpDOyhOvqYx3lKo5Nrluru7We4KxpsZJD816tUGnUfbvs.5VNDK', '超级管理员', 'admin@zhunong.com', '13800138000', 'ADMIN', 1);

-- 插入默认分类数据
INSERT IGNORE INTO `category` (`id`, `name`, `parent_id`, `level`, `sort_order`, `icon`, `status`) VALUES
(1, '新鲜水果', 0, 1, 1, 'fa-apple-alt', 1),
(2, '时令蔬菜', 0, 1, 2, 'fa-carrot', 1),
(3, '肉禽蛋品', 0, 1, 3, 'fa-drumstick-bite', 1),
(4, '水产海鲜', 0, 1, 4, 'fa-fish', 1),
(5, '粮油调味', 0, 1, 5, 'fa-seedling', 1),
(6, '苹果', 1, 2, 1, NULL, 1),
(7, '香蕉', 1, 2, 2, NULL, 1),
(8, '橙子', 1, 2, 3, NULL, 1),
(9, '叶菜类', 2, 2, 1, NULL, 1),
(10, '根茎类', 2, 2, 2, NULL, 1),
(11, '猪肉', 3, 2, 1, NULL, 1),
(12, '鸡肉', 3, 2, 2, NULL, 1);

-- 插入默认轮播图数据
INSERT IGNORE INTO `banner` (`title`, `image`, `link`, `sort_order`, `status`) VALUES
('新鲜水果', 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=1200&h=400&fit=crop', '/category/1', 1, 1),
('时令蔬菜', 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=1200&h=400&fit=crop', '/category/2', 2, 1),
('肉禽蛋品', 'https://images.unsplash.com/photo-1607623814075-e51df1bd656c?w=1200&h=400&fit=crop', '/category/3', 3, 1);

-- 插入测试用户账号（密码：user123）
INSERT IGNORE INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `phone`, `role`, `status`) VALUES
(2, 'user1', '$2a$10$kaHpDOyhOvqYx3lKo5Nrluru7We4KxpsZJD816tUGnUfbvs.5VNDK', '测试用户', 'user1@test.com', '13900139000', 'USER', 1),
(3, 'user2', '$2a$10$kaHpDOyhOvqYx3lKo5Nrluru7We4KxpsZJD816tUGnUfbvs.5VNDK', '张三', 'user2@test.com', '13900139001', 'USER', 1);

-- 插入商品测试数据
INSERT IGNORE INTO `product` (`id`, `name`, `category_id`, `main_image`, `images`, `price`, `original_price`, `stock`, `sales`, `description`, `detail`, `origin`, `unit`, `weight`, `status`, `is_hot`, `is_new`, `is_help`, `sort_order`) VALUES
(1, '红富士苹果', 6, 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400&h=400&fit=crop', '["https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400&h=400&fit=crop"]', 12.90, 15.90, 100, 50, '新鲜红富士苹果，脆甜多汁', '山东烟台红富士苹果，产地直发，新鲜采摘，口感脆甜，营养丰富。', '山东烟台', '500g', 0.5, 1, 1, 1, 1, 1),
(2, '进口香蕉', 7, 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400&h=400&fit=crop', '["https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400&h=400&fit=crop"]', 8.90, 10.90, 200, 80, '进口香蕉，香甜软糯', '菲律宾进口香蕉，果肉细腻，香甜软糯，富含钾元素。', '菲律宾', '500g', 0.5, 1, 0, 1, 0, 2),
(3, '赣南脐橙', 8, 'https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?w=400&h=400&fit=crop', '["https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?w=400&h=400&fit=crop"]', 15.90, 19.90, 150, 60, '赣南脐橙，汁多味甜', '江西赣南脐橙，国家地理标志产品，果肉饱满，汁多味甜。', '江西赣州', '500g', 0.5, 1, 1, 0, 1, 3),
(4, '有机菠菜', 9, 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=400&h=400&fit=crop', '["https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=400&h=400&fit=crop"]', 6.90, 8.90, 80, 30, '有机菠菜，绿色健康', '有机种植菠菜，无农药残留，绿色健康，富含维生素。', '北京郊区', '300g', 0.3, 1, 0, 1, 1, 1),
(5, '胡萝卜', 10, 'https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400&h=400&fit=crop', '["https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400&h=400&fit=crop"]', 4.90, 6.90, 120, 40, '新鲜胡萝卜，甜脆可口', '新鲜胡萝卜，肉质细嫩，甜脆可口，富含胡萝卜素。', '河北保定', '500g', 0.5, 1, 0, 0, 0, 2),
(6, '五花肉', 11, 'https://images.unsplash.com/photo-1607623814075-e51df1bd656c?w=400&h=400&fit=crop', '["https://images.unsplash.com/photo-1607623814075-e51df1bd656c?w=400&h=400&fit=crop"]', 28.90, 32.90, 50, 25, '优质五花肉，肥瘦相间', '优质五花肉，肥瘦相间，肉质鲜嫩，适合红烧、炖煮。', '河南南阳', '500g', 0.5, 1, 1, 0, 1, 1),
(7, '土鸡蛋', 12, 'https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=400&h=400&fit=crop', '["https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=400&h=400&fit=crop"]', 18.90, 22.90, 200, 100, '散养土鸡蛋，营养丰富', '农家散养土鸡蛋，蛋黄饱满，营养丰富，口感香醇。', '湖北孝感', '10枚', 0.6, 1, 0, 1, 1, 2);

-- 插入测试订单数据
INSERT IGNORE INTO `orders` (`id`, `order_no`, `user_id`, `total_amount`, `freight_amount`, `discount_amount`, `pay_amount`, `pay_type`, `status`, `receiver_name`, `receiver_phone`, `receiver_address`, `remark`) VALUES
(1, '202403270001', 2, 45.70, 5.00, 0.00, 50.70, 1, 3, '张三', '13900139000', '北京市朝阳区测试路1号', '请尽快发货'),
(2, '202403270002', 2, 28.90, 5.00, 2.00, 31.90, 2, 2, '张三', '13900139000', '北京市朝阳区测试路1号', ''),
(3, '202403270003', 3, 66.60, 0.00, 5.00, 61.60, 1, 1, '李四', '13900139001', '上海市浦东新区测试路2号', '请包装好');

-- 插入订单商品数据
INSERT IGNORE INTO `order_item` (`id`, `order_id`, `product_id`, `product_name`, `product_image`, `price`, `quantity`, `total_amount`) VALUES
(1, 1, 1, '红富士苹果', 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400&h=400&fit=crop', 12.90, 2, 25.80),
(2, 1, 2, '进口香蕉', 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400&h=400&fit=crop', 8.90, 1, 8.90),
(3, 1, 4, '有机菠菜', 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=400&h=400&fit=crop', 6.90, 2, 13.80),
(4, 2, 6, '五花肉', 'https://images.unsplash.com/photo-1607623814075-e51df1bd656c?w=400&h=400&fit=crop', 28.90, 1, 28.90),
(5, 3, 1, '红富士苹果', 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400&h=400&fit=crop', 12.90, 2, 25.80),
(6, 3, 7, '土鸡蛋', 'https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=400&h=400&fit=crop', 18.90, 2, 37.80);

-- 插入测试购物车数据
INSERT IGNORE INTO `cart` (`id`, `user_id`, `product_id`, `quantity`, `selected`) VALUES
(1, 2, 3, 2, 1),
(2, 2, 5, 3, 1),
(3, 3, 2, 1, 1);

-- 插入测试收货地址
INSERT IGNORE INTO `address` (`id`, `user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail_address`, `is_default`) VALUES
(1, 2, '张三', '13900139000', '北京市', '北京市', '朝阳区', '测试路1号院1号楼101室', 1),
(2, 2, '张三', '13900139000', '北京市', '北京市', '海淀区', '中关村大街2号院2号楼202室', 0),
(3, 3, '李四', '13900139001', '上海市', '上海市', '浦东新区', '测试路2号院3号楼303室', 1);
