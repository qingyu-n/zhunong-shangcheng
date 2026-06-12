-- ============================================
-- 助农商城全新数据库建表脚本
-- 数据库名: zhunong-shangcheng
-- MySQL 8.0
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `zhunong-shangcheng` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `zhunong-shangcheng`;

-- ============================================
-- 一、系统基础表
-- ============================================

-- 1. 管理员表 (sys_admin)
DROP TABLE IF EXISTS sys_admin;
CREATE TABLE sys_admin (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 2. 普通用户表 (sys_user)
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
    birthday DATE DEFAULT NULL COMMENT '生日',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    UNIQUE KEY uk_phone (phone),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='普通用户表';

-- ============================================
-- 二、商品业务表
-- ============================================

-- 3. 商品分类表 (product_category)
DROP TABLE IF EXISTS product_category;
CREATE TABLE product_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID, 0为顶级分类',
    level TINYINT NOT NULL DEFAULT 1 COMMENT '层级: 1-一级 2-二级 3-三级',
    sort_order INT DEFAULT 0 COMMENT '排序',
    icon VARCHAR(255) DEFAULT NULL COMMENT '分类图标URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_level (level),
    KEY idx_status (status),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 4. 商品表 (product)
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    description TEXT COMMENT '商品描述',
    detail TEXT COMMENT '商品详情(富文本)',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    sales INT DEFAULT 0 COMMENT '销量',
    main_image VARCHAR(255) DEFAULT NULL COMMENT '主图URL(七牛云CDN)',
    images JSON COMMENT '商品图片列表(JSON数组,七牛云CDN地址)',
    origin VARCHAR(100) DEFAULT NULL COMMENT '产地',
    unit VARCHAR(20) DEFAULT '件' COMMENT '单位',
    weight DECIMAL(8,2) DEFAULT NULL COMMENT '重量(kg)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    is_hot TINYINT DEFAULT 0 COMMENT '是否热销: 0-否 1-是',
    is_new TINYINT DEFAULT 0 COMMENT '是否新品: 0-否 1-是',
    is_help TINYINT DEFAULT 0 COMMENT '是否助农产品: 0-否 1-是',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_status (status),
    KEY idx_is_hot (is_hot),
    KEY idx_is_new (is_new),
    KEY idx_is_help (is_help),
    KEY idx_sort_order (sort_order),
    KEY idx_price (price),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================
-- 三、用户业务表
-- ============================================

-- 5. 用户收货地址表 (user_address)
DROP TABLE IF EXISTS user_address;
CREATE TABLE user_address (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '地址ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    phone VARCHAR(20) NOT NULL COMMENT '收货人手机号',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    district VARCHAR(50) NOT NULL COMMENT '区县',
    detail_address VARCHAR(255) NOT NULL COMMENT '详细地址',
    zip_code VARCHAR(10) DEFAULT NULL COMMENT '邮编',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认: 0-否 1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_is_default (is_default),
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址表';

-- 6. 用户收藏表 (user_collect)
DROP TABLE IF EXISTS user_collect;
CREATE TABLE user_collect (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_product (user_id, product_id),
    KEY idx_user_id (user_id),
    KEY idx_product_id (product_id),
    CONSTRAINT fk_collect_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_collect_product FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- ============================================
-- 四、订单购物车表
-- ============================================

-- 7. 购物车表 (shopping_cart)
DROP TABLE IF EXISTS shopping_cart;
CREATE TABLE shopping_cart (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    selected TINYINT DEFAULT 1 COMMENT '是否选中: 0-否 1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_product (user_id, product_id),
    KEY idx_user_id (user_id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- 8. 订单主表 (order_info)
DROP TABLE IF EXISTS order_info;
CREATE TABLE order_info (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    freight_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '运费',
    discount_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '应付金额',
    pay_type TINYINT DEFAULT NULL COMMENT '支付方式: 1-微信支付 2-支付宝 3-银行卡',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待付款 1-待发货 2-待收货 3-已完成 4-已取消',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
    receiver_address VARCHAR(500) NOT NULL COMMENT '收货地址',
    remark VARCHAR(500) DEFAULT NULL COMMENT '订单备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

-- 9. 订单明细表 (order_item)
DROP TABLE IF EXISTS order_item;
CREATE TABLE order_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    product_image VARCHAR(255) DEFAULT NULL COMMENT '商品图片URL',
    product_price DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    quantity INT NOT NULL COMMENT '数量',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_product_id (product_id),
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES order_info(id),
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- ============================================
-- 五、内容运营表
-- ============================================

-- 10. 轮播图表 (carousel)
DROP TABLE IF EXISTS carousel;
CREATE TABLE carousel (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
    title VARCHAR(100) DEFAULT NULL COMMENT '标题',
    subtitle VARCHAR(200) DEFAULT NULL COMMENT '副标题',
    image VARCHAR(255) NOT NULL COMMENT '图片URL(七牛云CDN)',
    link VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

-- 11. 助农活动表 (activity)
DROP TABLE IF EXISTS activity;
CREATE TABLE activity (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    title VARCHAR(100) NOT NULL COMMENT '活动标题',
    subtitle VARCHAR(200) DEFAULT NULL COMMENT '活动副标题',
    description TEXT COMMENT '活动描述',
    image VARCHAR(255) DEFAULT NULL COMMENT '活动图片URL(七牛云CDN)',
    banner_image VARCHAR(255) DEFAULT NULL COMMENT '活动横幅图片URL',
    link VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_sort_order (sort_order),
    KEY idx_start_time (start_time),
    KEY idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='助农活动表';

-- 设置自增ID起始值
ALTER TABLE sys_admin AUTO_INCREMENT = 100;
ALTER TABLE sys_user AUTO_INCREMENT = 1000;
ALTER TABLE product_category AUTO_INCREMENT = 100;
ALTER TABLE product AUTO_INCREMENT = 1000;
ALTER TABLE user_address AUTO_INCREMENT = 100;
ALTER TABLE user_collect AUTO_INCREMENT = 100;
ALTER TABLE shopping_cart AUTO_INCREMENT = 100;
ALTER TABLE order_info AUTO_INCREMENT = 10000;
ALTER TABLE order_item AUTO_INCREMENT = 100;
ALTER TABLE carousel AUTO_INCREMENT = 100;
ALTER TABLE activity AUTO_INCREMENT = 100;

-- 输出完成信息
SELECT '数据库表创建完成！共创建11张表' AS message;
