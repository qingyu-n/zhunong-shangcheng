package com.zhunong.mall.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class IsDeleteFieldInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        // 修复sys_user表缺少的农户相关字段
        try {
            jdbcTemplate.execute("ALTER TABLE `sys_user` ADD COLUMN `is_farmer` TINYINT NOT NULL DEFAULT 0 COMMENT '是否是农户: 0-否 1-是' AFTER `status`");
            System.out.println("✓ 成功为表 sys_user 添加 is_farmer 字段");
        } catch (Exception e) {
            System.out.println("× 为表 sys_user 添加 is_farmer 字段时：" + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE `sys_user` ADD COLUMN `farmer_apply_status` TINYINT DEFAULT NULL COMMENT '农户申请状态: 0-待审核 1-已通过 2-已拒绝' AFTER `is_farmer`");
            System.out.println("✓ 成功为表 sys_user 添加 farmer_apply_status 字段");
        } catch (Exception e) {
            System.out.println("× 为表 sys_user 添加 farmer_apply_status 字段时：" + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE `sys_user` ADD COLUMN `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名' AFTER `farmer_apply_status`");
            System.out.println("✓ 成功为表 sys_user 添加 real_name 字段");
        } catch (Exception e) {
            System.out.println("× 为表 sys_user 添加 real_name 字段时：" + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE `sys_user` ADD COLUMN `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号' AFTER `real_name`");
            System.out.println("✓ 成功为表 sys_user 添加 id_card 字段");
        } catch (Exception e) {
            System.out.println("× 为表 sys_user 添加 id_card 字段时：" + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE `sys_user` ADD COLUMN `birthday` VARCHAR(20) DEFAULT NULL COMMENT '生日' AFTER `id_card`");
            System.out.println("✓ 成功为表 sys_user 添加 birthday 字段");
        } catch (Exception e) {
            System.out.println("× 为表 sys_user 添加 birthday 字段时：" + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE `sys_user` ADD COLUMN `gender` TINYINT DEFAULT NULL COMMENT '性别: 0-女 1-男' AFTER `birthday`");
            System.out.println("✓ 成功为表 sys_user 添加 gender 字段");
        } catch (Exception e) {
            System.out.println("× 为表 sys_user 添加 gender 字段时：" + e.getMessage());
        }

        // 修复order_item表缺少的字段
        try {
            jdbcTemplate.execute("ALTER TABLE `order_item` ADD COLUMN `product_price` DECIMAL(10,2) COMMENT '商品单价' AFTER `product_image`");
            System.out.println("✓ 成功为表 order_item 添加 product_price 字段");
        } catch (Exception e) {
            System.out.println("× 为表 order_item 添加 product_price 字段时：" + e.getMessage());
        }

        // 修复review表字段
        try {
            jdbcTemplate.execute("ALTER TABLE `product_review` ADD COLUMN `reply_content` TEXT COMMENT '商家回复内容' AFTER `content`");
            System.out.println("✓ 成功为表 product_review 添加 reply_content 字段");
        } catch (Exception e) {
            System.out.println("× 为表 product_review 添加 reply_content 字段时：" + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE `product_review` ADD COLUMN `reply_time` DATETIME COMMENT '商家回复时间' AFTER `reply_content`");
            System.out.println("✓ 成功为表 product_review 添加 reply_time 字段");
        } catch (Exception e) {
            System.out.println("× 为表 product_review 添加 reply_time 字段时：" + e.getMessage());
        }

        // 修复farmer_profile表缺少detail_address字段
        try {
            jdbcTemplate.execute("ALTER TABLE `farmer_profile` ADD COLUMN `detail_address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址' AFTER `district`");
            System.out.println("✓ 成功为表 farmer_profile 添加 detail_address 字段");
        } catch (Exception e) {
            System.out.println("× 为表 farmer_profile 添加 detail_address 字段时：" + e.getMessage());
        }

        String[] tables = {
            "product",
            "shopping_cart",
            "category",
            "sys_user",
            "banner",
            "order_info",
            "order_item",
            "address",
            "farmer_profile",
            "product_audit",
            "product_report",
            "product_review",
            "shop",
            "shop_favorite",
            "special_product",
            "favorite",
            "message",
            "activity",
            "product_detail",
            "admin"
        };

        for (String table : tables) {
            try {
                String sql = "ALTER TABLE `" + table + "` ADD COLUMN `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是' AFTER `update_time`";
                jdbcTemplate.execute(sql);
                System.out.println("✓ 成功为表 " + table + " 添加 is_delete 字段");
            } catch (Exception e) {
                System.out.println("× 为表 " + table + " 添加 is_delete 字段时：" + e.getMessage());
            }
        }

        System.out.println("\n========================================");
        System.out.println("数据库字段初始化完成！");
        System.out.println("========================================");
    }
}
