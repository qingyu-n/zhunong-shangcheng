package com.zhunong.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhunong.mall.common.BaseEntity;

/**
 * 收藏实体类 - 对应 user_collect 表
 */
@TableName("user_collect")
public class Favorite extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long productId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
