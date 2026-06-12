CREATE TABLE IF NOT EXISTS farmer_profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    shop_name VARCHAR(100) NOT NULL,
    shop_logo VARCHAR(500) DEFAULT NULL,
    description TEXT DEFAULT NULL,
    business_license VARCHAR(500) DEFAULT NULL,
    contact_name VARCHAR(50) DEFAULT NULL,
    contact_phone VARCHAR(20) DEFAULT NULL,
    contact_address VARCHAR(255) DEFAULT NULL,
    province VARCHAR(50) DEFAULT NULL,
    city VARCHAR(50) DEFAULT NULL,
    district VARCHAR(50) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    audit_remark VARCHAR(500) DEFAULT NULL,
    auditor_id BIGINT DEFAULT NULL,
    audit_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_status (status),
    KEY idx_auditor_id (auditor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_audit (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    farmer_id BIGINT NOT NULL,
    auditor_id BIGINT DEFAULT NULL,
    audit_status TINYINT NOT NULL,
    audit_remark VARCHAR(500) DEFAULT NULL,
    audit_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_product_id (product_id),
    KEY idx_farmer_id (farmer_id),
    KEY idx_auditor_id (auditor_id),
    KEY idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS shop (
    id BIGINT NOT NULL AUTO_INCREMENT,
    farmer_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    logo VARCHAR(500) DEFAULT NULL,
    banner_image VARCHAR(500) DEFAULT NULL,
    description TEXT DEFAULT NULL,
    contact_phone VARCHAR(20) DEFAULT NULL,
    contact_wechat VARCHAR(100) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    view_count INT NOT NULL DEFAULT 0,
    favorite_count INT NOT NULL DEFAULT 0,
    product_count INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_farmer_id (farmer_id),
    KEY idx_status (status),
    KEY idx_view_count (view_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    type TINYINT NOT NULL DEFAULT 1,
    related_id BIGINT DEFAULT NULL,
    related_type VARCHAR(50) DEFAULT NULL,
    is_read TINYINT NOT NULL DEFAULT 0,
    read_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_type (type),
    KEY idx_is_read (is_read),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_review (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    farmer_id BIGINT NOT NULL,
    rating TINYINT NOT NULL,
    content TEXT DEFAULT NULL,
    images JSON DEFAULT NULL,
    reply_content TEXT DEFAULT NULL,
    reply_time DATETIME DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_product_id (product_id),
    KEY idx_order_id (order_id),
    KEY idx_user_id (user_id),
    KEY idx_farmer_id (farmer_id),
    KEY idx_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product_report (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason_type TINYINT NOT NULL,
    reason_detail TEXT NOT NULL,
    evidence_images JSON DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    handler_id BIGINT DEFAULT NULL,
    handle_result VARCHAR(500) DEFAULT NULL,
    handle_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_product_id (product_id),
    KEY idx_reporter_id (reporter_id),
    KEY idx_status (status),
    KEY idx_handler_id (handler_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS shop_favorite (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    shop_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_shop (user_id, shop_id),
    KEY idx_user_id (user_id),
    KEY idx_shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
