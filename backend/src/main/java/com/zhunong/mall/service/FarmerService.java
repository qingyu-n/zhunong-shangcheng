package com.zhunong.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.FarmerProfile;
import com.zhunong.mall.entity.Product;
import com.zhunong.mall.common.PageResult;

public interface FarmerService extends IService<FarmerProfile> {

    Result<FarmerProfile> applyFarmer(Long userId, FarmerProfile profile);

    FarmerProfile getFarmerProfileByUserId(Long userId);

    Result<FarmerProfile> updateFarmerProfile(Long userId, FarmerProfile profile);

    Integer getApplyStatus(Long userId);

    Result<Product> publishProduct(Long userId, Product product);

    Result<PageResult<Product>> getMyProducts(Long userId, Integer page, Integer size, String keyword, Integer auditStatus, Integer status);

    Result<Void> updateProduct(Long userId, Long productId, Product product);

    Result<Void> deleteProduct(Long userId, Long productId);

    Result<Void> updateProductStatus(Long userId, Long productId, Integer status);

    Result<Product> getMyProductDetail(Long userId, Long productId);
}
