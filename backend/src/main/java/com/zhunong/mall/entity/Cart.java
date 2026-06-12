package com.zhunong.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhunong.mall.common.BaseEntity;

/**
 * 购物车实体类 - 对应 shopping_cart 表
 */
@TableName("shopping_cart")
public class Cart extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long productId;
    private Integer quantity;
    private Integer selected;

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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getSelected() {
        return selected;
    }

    public void setSelected(Integer selected) {
        this.selected = selected;
    }
}
