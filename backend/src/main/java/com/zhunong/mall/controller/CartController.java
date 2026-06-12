package com.zhunong.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.Cart;
import com.zhunong.mall.entity.Product;
import com.zhunong.mall.mapper.CartMapper;
import com.zhunong.mall.mapper.ProductMapper;
import com.zhunong.mall.utils.JwtUtil;
import com.zhunong.mall.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/list")
    public Result<List<CartVO>> getCartList(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return Result.error(401, "未登录");
        }
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.orderByDesc(Cart::getCreateTime);
        List<Cart> cartList = cartMapper.selectList(wrapper);

        List<CartVO> result = new ArrayList<>();
        for (Cart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product != null) {
                CartVO vo = new CartVO();
                vo.setId(cart.getId());
                vo.setUserId(cart.getUserId());
                vo.setProductId(cart.getProductId());
                vo.setName(product.getName());
                vo.setImage(product.getMainImage());
                vo.setOrigin(product.getOrigin());
                vo.setPrice(product.getPrice());
                vo.setQuantity(cart.getQuantity());
                vo.setSelected(cart.getSelected() != null && cart.getSelected() == 1);
                result.add(vo);
            }
        }
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<Void> addToCart(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                   @RequestBody Map<String, Object> data) {
        try {
            if (authHeader == null || authHeader.isEmpty()) {
                return Result.error(401, "未登录");
            }
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);

            if (data.get("productId") == null) {
                return Result.error(400, "商品ID不能为空");
            }

            Long productId = Long.valueOf(data.get("productId").toString());
            Integer quantity = data.get("quantity") != null ? Integer.valueOf(data.get("quantity").toString()) : 1;

            // 验证商品是否存在
            Product product = productMapper.selectById(productId);
            if (product == null || product.getStatus() != 1) {
                return Result.error(404, "商品不存在或已下架");
            }

            LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Cart::getUserId, userId);
            wrapper.eq(Cart::getProductId, productId);
            Cart existingCart = cartMapper.selectOne(wrapper);

            if (existingCart != null) {
                existingCart.setQuantity(existingCart.getQuantity() + quantity);
                cartMapper.updateById(existingCart);
            } else {
                Cart cart = new Cart();
                cart.setUserId(userId);
                cart.setProductId(productId);
                cart.setQuantity(quantity);
                cart.setSelected(0);
                cartMapper.insert(cart);
            }
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "添加购物车失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Void> updateCartItem(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                        @PathVariable Long id,
                                        @RequestBody Map<String, Object> data) {
        if (authHeader == null || authHeader.isEmpty()) {
            return Result.error(401, "未登录");
        }
        Cart cart = cartMapper.selectById(id);
        if (cart == null) {
            return Result.error(404, "购物车项不存在");
        }

        if (data.containsKey("quantity")) {
            cart.setQuantity(Integer.valueOf(data.get("quantity").toString()));
        }
        if (data.containsKey("selected")) {
            cart.setSelected((Boolean) data.get("selected") ? 1 : 0);
        }
        cartMapper.updateById(cart);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCartItem(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                        @PathVariable Long id) {
        if (authHeader == null || authHeader.isEmpty()) {
            return Result.error(401, "未登录");
        }
        cartMapper.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<Void> clearCart(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return Result.error(401, "未登录");
        }
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        cartMapper.delete(wrapper);
        return Result.success();
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                    @RequestBody Map<String, List<Long>> data) {
        if (authHeader == null || authHeader.isEmpty()) {
            return Result.error(401, "未登录");
        }
        List<Long> ids = data.get("ids");
        if (ids != null && !ids.isEmpty()) {
            cartMapper.deleteBatchIds(ids);
        }
        return Result.success();
    }
}
