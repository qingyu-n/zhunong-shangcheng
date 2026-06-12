package com.zhunong.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.Banner;
import com.zhunong.mall.entity.Product;
import com.zhunong.mall.mapper.BannerMapper;
import com.zhunong.mall.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页控制器
 *
 * @author 助农商城开发团队
 * @since 2026-03-22
 */
@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private BannerMapper bannerMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 获取轮播图列表
     *
     * @return 轮播图列表
     */
    @GetMapping("/banners")
    public Result<List<Banner>> getBanners() {
        List<Banner> banners = bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, 1)
                        .orderByAsc(Banner::getSort)
        );
        return Result.success(banners);
    }

    /**
     * 获取热销商品
     *
     * @param limit 返回数量
     * @return 商品列表
     */
    @GetMapping("/hot-products")
    public Result<List<Product>> getHotProducts(
            @RequestParam(defaultValue = "8") Integer limit) {
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .eq(Product::getIsHot, 1)
                        .orderByDesc(Product::getSales)
                        .last("LIMIT " + limit)
        );
        return Result.success(products);
    }
}
