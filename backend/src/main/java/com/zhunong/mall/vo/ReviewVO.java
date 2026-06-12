package com.zhunong.mall.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品评价视图对象
 */
@Data
public class ReviewVO {

    /** 评价ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 订单ID */
    private Long orderId;

    /** 评价用户ID（买家） */
    private Long userId;

    /** 评价用户名 */
    private String userName;

    /** 用户头像 */
    private String userAvatar;

    /** 农户用户ID（卖家） */
    private Long farmerId;

    /** 评分: 1-5星 */
    private Integer rating;

    /** 评价内容 */
    private String content;

    /** 评价图片列表(JSON数组) */
    private List<String> images;

    /** 农户回复内容 */
    private String replyContent;

    /** 回复时间 */
    private LocalDateTime replyTime;

    /** 状态: 0-隐藏 1-显示 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
