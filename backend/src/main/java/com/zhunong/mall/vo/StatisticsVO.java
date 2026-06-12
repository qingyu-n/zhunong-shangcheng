package com.zhunong.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统计数据视图对象
 */
@Data
@SuppressWarnings("unused")
public class StatisticsVO {

    /**
     * 数据概览
     */
    public static class Overview {
        /** 总销售额 */
        private BigDecimal totalSales;

        /** 总订单数 */
        private Integer totalOrders;

        /** 总商品数 */
        private Integer totalProducts;

        /** 店铺访问量 */
        private Integer totalViews;

        /** 今日销售额 */
        private BigDecimal todaySales;

        /** 今日订单数 */
        private Integer todayOrders;

        /** 今日新增访客 */
        private Integer todayVisitors;
    }

    /**
     * 销售趋势数据项
     */
    public static class SalesTrendItem {
        /** 日期 */
        private String date;

        /** 销售额 */
        private BigDecimal sales;

        /** 订单数 */
        private Integer orders;

        /** 商品数 */
        private Integer products;

        /** 环比增长率(%) */
        private Double growth;
    }

    /**
     * 商品统计数据
     */
    public static class ProductStats {
        /** 热销商品列表 */
        private List<TopProduct> topProducts;

        /** 订单状态分布 */
        private Map<Integer, Integer> orderStats;
    }

    /**
     * 热销商品
     */
    public static class TopProduct {
        /** 商品ID */
        private Long productId;

        /** 商品名称 */
        private String name;

        /** 商品主图 */
        private String mainImage;

        /** 销量 */
        private Integer salesCount;

        /** 销售额 */
        private BigDecimal salesAmount;
    }

    /** 数据概览 */
    private Overview overview;

    /** 销售趋势列表 */
    private List<SalesTrendItem> salesTrendList;

    /** 商品统计 */
    private ProductStats productStats;
}
