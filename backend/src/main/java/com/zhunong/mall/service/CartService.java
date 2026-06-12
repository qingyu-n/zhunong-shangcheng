package com.zhunong.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.Cart;

public interface CartService extends IService<Cart> {
    Result<?> addToCart(Long userId, Long productId, Integer quantity);
}
