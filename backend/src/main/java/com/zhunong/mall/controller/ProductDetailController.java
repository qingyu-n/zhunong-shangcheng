package com.zhunong.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhunong.mall.common.PageResult;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.Product;
import com.zhunong.mall.entity.ProductDetail;
import com.zhunong.mall.mapper.ProductDetailMapper;
import com.zhunong.mall.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/product-detail")
public class ProductDetailController {

    @Autowired
    private ProductDetailMapper productDetailMapper;

    @Autowired
    private ProductMapper productMapper;

    @GetMapping("/list")
    public Result<PageResult<ProductDetail>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        try {
            LambdaQueryWrapper<ProductDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductDetail::getIsDelete, 0);
            
            if (keyword != null && !keyword.isEmpty()) {
                // 通过商品名称搜索
                List<Product> products = productMapper.selectList(
                    new LambdaQueryWrapper<Product>()
                        .like(Product::getName, keyword)
                );
                if (!products.isEmpty()) {
                    List<Long> productIds = products.stream().map(Product::getId).toList();
                    wrapper.in(ProductDetail::getProductId, productIds);
                }
            }
            
            wrapper.orderByDesc(ProductDetail::getCreateTime);
            Page<ProductDetail> page = new Page<>(current, size);
            Page<ProductDetail> result = productDetailMapper.selectPage(page, wrapper);
            
            return Result.success(PageResult.of(result.getRecords(), result.getTotal()));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取商品详情列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<ProductDetail> getById(@PathVariable Long id) {
        try {
            ProductDetail detail = productDetailMapper.selectById(id);
            if (detail == null) {
                return Result.error(404, "商品详情不存在");
            }
            return Result.success(detail);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取商品详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/product/{productId}")
    public Result<Map<String, Object>> getByProductId(@PathVariable Long productId) {
        try {
            ProductDetail detail = productDetailMapper.selectByProductId(productId);
            if (detail == null) {
                return Result.error(404, "商品详情不存在");
            }
            
            // 获取商品信息
            Product product = productMapper.selectById(productId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", detail.getId());
            result.put("productId", detail.getProductId());
            result.put("description", detail.getDescription());
            result.put("farmerStory", detail.getFarmerStory());
            result.put("farmerImage", detail.getFarmerImage());
            result.put("images", List.of(
                detail.getImage1(), detail.getImage2(), detail.getImage3(), 
                detail.getImage4(), detail.getImage5()
            ).stream().filter(img -> img != null && !img.isEmpty()).toList());
            result.put("params", List.of(
                Map.of("label", "商品名称", "value", detail.getParamName() != null ? detail.getParamName() : (product != null ? product.getName() : "")),
                Map.of("label", "产地", "value", detail.getParamOrigin() != null ? detail.getParamOrigin() : ""),
                Map.of("label", "保质期", "value", detail.getParamShelfLife() != null ? detail.getParamShelfLife() : ""),
                Map.of("label", "储存方式", "value", detail.getParamStorage() != null ? detail.getParamStorage() : ""),
                Map.of("label", "发货时间", "value", detail.getParamDelivery() != null ? detail.getParamDelivery() : "")
            ));
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取商品详情失败: " + e.getMessage());
        }
    }

    @PostMapping
    public Result<ProductDetail> create(@RequestBody ProductDetail detail) {
        try {
            detail.setCreateTime(LocalDateTime.now());
            detail.setUpdateTime(LocalDateTime.now());
            detail.setStatus(1);
            detail.setIsDelete(0);
            productDetailMapper.insert(detail);
            return Result.success(detail);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "创建商品详情失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<ProductDetail> update(@PathVariable Long id, @RequestBody ProductDetail detail) {
        try {
            detail.setId(id);
            detail.setUpdateTime(LocalDateTime.now());
            productDetailMapper.updateById(detail);
            return Result.success(detail);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "更新商品详情失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            ProductDetail detail = new ProductDetail();
            detail.setId(id);
            detail.setIsDelete(1);
            detail.setUpdateTime(LocalDateTime.now());
            productDetailMapper.updateById(detail);
            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "删除商品详情失败: " + e.getMessage());
        }
    }
}
