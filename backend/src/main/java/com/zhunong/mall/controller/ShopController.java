package com.zhunong.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhunong.mall.common.PageResult;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.Shop;
import com.zhunong.mall.entity.ShopFavorite;
import com.zhunong.mall.mapper.ProductMapper;
import com.zhunong.mall.mapper.ShopFavoriteMapper;
import com.zhunong.mall.mapper.ShopMapper;
import com.zhunong.mall.entity.Product;
import com.zhunong.mall.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/shops")
public class ShopController {

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private ShopFavoriteMapper shopFavoriteMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromHeader(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }

    @GetMapping("/{id}")
    public Result<Shop> getShopDetail(@PathVariable Long id,
                                       @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Shop shop = shopMapper.selectById(id);
        if (shop == null) {
            return Result.error(404, "店铺不存在");
        }
        
        shop.setViewCount(shop.getViewCount() + 1);
        shopMapper.updateById(shop);
        
        return Result.success(shop);
    }

    @GetMapping("/{id}/products")
    public Result<PageResult<Product>> getShopProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getFarmerId, id)
               .eq(Product::getStatus, 1)
               .eq(Product::getAuditStatus, 1)
               .orderByDesc(Product::getCreateTime);
        
        Page<Product> result = productMapper.selectPage(pageParam, wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @PostMapping("/{id}/favorite")
    public Result<Void> favoriteShop(@PathVariable Long id,
                                      @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        LambdaQueryWrapper<ShopFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopFavorite::getUserId, userId).eq(ShopFavorite::getShopId, id);
        if (shopFavoriteMapper.selectCount(wrapper) > 0) {
            return Result.error("已收藏该店铺");
        }
        
        ShopFavorite favorite = new ShopFavorite();
        favorite.setUserId(userId);
        favorite.setShopId(id);
        favorite.setCreateTime(LocalDateTime.now());
        shopFavoriteMapper.insert(favorite);
        
        Shop shop = shopMapper.selectById(id);
        if (shop != null) {
            shop.setFavoriteCount(shop.getFavoriteCount() + 1);
            shopMapper.updateById(shop);
        }
        
        return Result.success("收藏成功", null);
    }

    @DeleteMapping("/{id}/favorite")
    public Result<Void> unfavoriteShop(@PathVariable Long id,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        LambdaQueryWrapper<ShopFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopFavorite::getUserId, userId).eq(ShopFavorite::getShopId, id);
        shopFavoriteMapper.delete(wrapper);
        
        Shop shop = shopMapper.selectById(id);
        if (shop != null && shop.getFavoriteCount() > 0) {
            shop.setFavoriteCount(shop.getFavoriteCount() - 1);
            shopMapper.updateById(shop);
        }
        
        return Result.success("取消收藏成功", null);
    }

    @GetMapping("/my-favorites")
    public Result<PageResult<Shop>> getFavoriteShops(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(PageResult.of(java.util.Collections.emptyList(), 0L));
    }

    @PutMapping("/my")
    public Result<Shop> updateMyShop(@RequestBody Shop shopData,
                                      @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getFarmerId, userId);
        Shop shop = shopMapper.selectOne(wrapper);
        
        if (shop == null) {
            shop = new Shop();
            shop.setFarmerId(userId);
            shop.setName(shopData.getName());
            shop.setLogo(shopData.getLogo());
            shop.setBannerImage(shopData.getBannerImage());
            shop.setDescription(shopData.getDescription());
            shop.setContactPhone(shopData.getContactPhone());
            shop.setContactWechat(shopData.getContactWechat());
            shop.setAddress(shopData.getAddress());
            shop.setViewCount(0);
            shop.setFavoriteCount(0);
            shop.setProductCount(0);
            shop.setStatus(1);
            shopMapper.insert(shop);
        } else {
            if (shopData.getName() != null) shop.setName(shopData.getName());
            if (shopData.getLogo() != null) shop.setLogo(shopData.getLogo());
            if (shopData.getBannerImage() != null) shop.setBannerImage(shopData.getBannerImage());
            if (shopData.getDescription() != null) shop.setDescription(shopData.getDescription());
            if (shopData.getContactPhone() != null) shop.setContactPhone(shopData.getContactPhone());
            if (shopData.getContactWechat() != null) shop.setContactWechat(shopData.getContactWechat());
            if (shopData.getAddress() != null) shop.setAddress(shopData.getAddress());
            shopMapper.updateById(shop);
        }
        
        return Result.success("保存成功", shop);
    }

    @GetMapping("/my")
    public Result<Shop> getMyShop(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getFarmerId, userId);
        Shop shop = shopMapper.selectOne(wrapper);
        
        if (shop == null) {
            return Result.error(404, "店铺不存在");
        }
        
        return Result.success(shop);
    }
}
