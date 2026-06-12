package com.zhunong.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.Cart;
import com.zhunong.mall.mapper.CartMapper;
import com.zhunong.mall.service.CartService;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Override
    public Result<?> addToCart(Long userId, Long productId, Integer quantity) {
        try {
            if (userId == null || productId == null || quantity == null || quantity <= 0) {
                return Result.error("参数错误");
            }

            LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Cart::getUserId, userId)
                        .eq(Cart::getProductId, productId)
                        .eq(Cart::getIsDelete, 0);
            
            Cart existingCart = baseMapper.selectOne(queryWrapper);
            
            if (existingCart != null) {
                existingCart.setQuantity(existingCart.getQuantity() + quantity);
                baseMapper.updateById(existingCart);
                return Result.success("购物车商品数量已更新");
            } else {
                Cart newCart = new Cart();
                newCart.setUserId(userId);
                newCart.setProductId(productId);
                newCart.setQuantity(quantity);
                newCart.setSelected(1);
                baseMapper.insert(newCart);
                return Result.success("商品已添加到购物车");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加购物车失败: " + e.getMessage());
        }
    }
}
