package com.zhunong.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhunong.mall.common.PageResult;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.Favorite;
import com.zhunong.mall.entity.Product;
import com.zhunong.mall.mapper.FavoriteMapper;
import com.zhunong.mall.mapper.ProductMapper;
import com.zhunong.mall.utils.JwtUtil;
import com.zhunong.mall.vo.FavoriteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取收藏列表
     */
    @GetMapping("/list")
    public Result<PageResult<FavoriteVO>> getFavoriteList(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getIsDelete, 0);
        wrapper.orderByDesc(Favorite::getCreateTime);
        
        Page<Favorite> pageParam = new Page<>(page, size);
        Page<Favorite> result = favoriteMapper.selectPage(pageParam, wrapper);
        
        List<FavoriteVO> voList = new ArrayList<>();
        for (Favorite favorite : result.getRecords()) {
            Product product = productMapper.selectById(favorite.getProductId());
            if (product != null && product.getIsDelete() == 0) {
                FavoriteVO vo = new FavoriteVO();
                vo.setId(favorite.getId());
                vo.setProductId(product.getId());
                vo.setName(product.getName());
                vo.setMainImage(product.getMainImage());
                vo.setPrice(product.getPrice());
                vo.setOrigin(product.getOrigin());
                vo.setCreateTime(favorite.getCreateTime());
                voList.add(vo);
            }
        }
        
        return Result.success(PageResult.of(voList, result.getTotal()));
    }

    /**
     * 添加收藏
     */
    @PostMapping
    public Result<Void> addFavorite(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Favorite favorite) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        // 检查商品是否存在
        Product product = productMapper.selectById(favorite.getProductId());
        if (product == null || product.getIsDelete() == 1) {
            return Result.error(404, "商品不存在");
        }

        // 检查是否已收藏
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getProductId, favorite.getProductId());
        wrapper.eq(Favorite::getIsDelete, 0);
        
        Favorite existing = favoriteMapper.selectOne(wrapper);
        if (existing != null) {
            return Result.error(400, "已收藏该商品");
        }

        favorite.setUserId(userId);
        favoriteMapper.insert(favorite);
        return Result.success();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping
    public Result<Void> removeFavorite(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Long productId) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getProductId, productId);
        wrapper.eq(Favorite::getIsDelete, 0);
        
        Favorite favorite = favoriteMapper.selectOne(wrapper);
        if (favorite == null) {
            return Result.error(404, "收藏记录不存在");
        }

        favorite.setIsDelete(1);
        favoriteMapper.updateById(favorite);
        return Result.success();
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Long productId) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getProductId, productId);
        wrapper.eq(Favorite::getIsDelete, 0);
        
        Long count = favoriteMapper.selectCount(wrapper);
        return Result.success(count > 0);
    }

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        try {
            String token = authHeader.replace("Bearer ", "");
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}
