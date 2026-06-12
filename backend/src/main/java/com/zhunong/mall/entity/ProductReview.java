package com.zhunong.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhunong.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@TableName("product_review")
@Data
@EqualsAndHashCode(callSuper = false)
public class ProductReview extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long productId;

    private Long orderId;

    private Long userId;

    private Long farmerId;

    private Integer rating;

    private String content;

    private String images;

    private String replyContent;

    private LocalDateTime replyTime;

    private Integer status;
}
