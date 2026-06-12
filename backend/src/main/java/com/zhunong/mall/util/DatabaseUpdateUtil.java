package com.zhunong.mall.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * 数据库更新工具类 - 用于添加is_delete字段
 */
public class DatabaseUpdateUtil {

    private static final String URL = System.getenv().getOrDefault("DB_URL",
            "jdbc:mysql://localhost:3306/zhunong-shangcheng?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true");
    private static final String USERNAME = System.getenv().getOrDefault("DB_USERNAME", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");

    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            Statement stmt = conn.createStatement();

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
                    stmt.executeUpdate(sql);
                    System.out.println("成功为表 " + table + " 添加 is_delete 字段");
                } catch (Exception e) {
                    System.out.println("为表 " + table + " 添加 is_delete 字段时出错：" + e.getMessage());
                }
            }

            stmt.close();
            conn.close();
            System.out.println("\n数据库更新完成！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
