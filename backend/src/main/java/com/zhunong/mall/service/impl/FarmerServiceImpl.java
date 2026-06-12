package com.zhunong.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhunong.mall.common.PageResult;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.FarmerProfile;
import com.zhunong.mall.entity.Product;
import com.zhunong.mall.mapper.FarmerProfileMapper;
import com.zhunong.mall.mapper.ProductMapper;
import com.zhunong.mall.mapper.UserMapper;
import com.zhunong.mall.entity.User;
import com.zhunong.mall.service.FarmerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FarmerServiceImpl extends ServiceImpl<FarmerProfileMapper, FarmerProfile> implements FarmerService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<FarmerProfile> applyFarmer(Long userId, FarmerProfile profile) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if (user.getIsFarmer() != null && user.getIsFarmer() == 1) {
            return Result.error("您已经是农户，无需重复申请");
        }

        LambdaQueryWrapper<FarmerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FarmerProfile::getUserId, userId)
               .eq(FarmerProfile::getIsDelete, 0);
        FarmerProfile existing = baseMapper.selectOne(wrapper);
        
        if (existing != null && existing.getStatus() == 0) {
            return Result.error("您有待审核的申请，请耐心等待");
        }

        profile.setUserId(userId);
        profile.setStatus(0);
        
        if (existing != null) {
            profile.setId(existing.getId());
            baseMapper.updateById(profile);
        } else {
            baseMapper.insert(profile);
        }

        user.setFarmerApplyStatus(0);
        userMapper.updateById(user);

        return Result.success("申请提交成功，请等待审核", profile);
    }

    @Override
    public FarmerProfile getFarmerProfileByUserId(Long userId) {
        LambdaQueryWrapper<FarmerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FarmerProfile::getUserId, userId)
               .eq(FarmerProfile::getIsDelete, 0);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<FarmerProfile> updateFarmerProfile(Long userId, FarmerProfile profile) {
        FarmerProfile existing = getFarmerProfileByUserId(userId);
        if (existing == null) {
            return Result.error(404, "农户资料不存在");
        }
        
        profile.setId(existing.getId());
        profile.setUserId(userId);
        profile.setStatus(null);
        profile.setAuditRemark(null);
        profile.setAuditorId(null);
        profile.setAuditTime(null);
        baseMapper.updateById(profile);
        
        return Result.success("资料更新成功", profile);
    }

    @Override
    public Integer getApplyStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            return user.getFarmerApplyStatus();
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Product> publishProduct(Long userId, Product product) {
        User user = userMapper.selectById(userId);
        if (user == null || (user.getIsFarmer() == null || user.getIsFarmer() != 1)) {
            return Result.error("您还不是认证农户，无法发布商品");
        }

        product.setFarmerId(userId);
        product.setProductType(1);
        product.setAuditStatus(0);
        product.setStatus(0);
        product.setRejectReason(null);
        
        productMapper.insert(product);
        
        return Result.success("商品发布成功，等待审核", product);
    }

    @Override
    public Result<PageResult<Product>> getMyProducts(Long userId, Integer page, Integer size, String keyword, Integer auditStatus, Integer status) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getFarmerId, userId)
               .eq(Product::getIsDelete, 0)
               .like(keyword != null && !keyword.isEmpty(), Product::getName, keyword)
               .eq(auditStatus != null, Product::getAuditStatus, auditStatus)
               .eq(status != null, Product::getStatus, status)
               .orderByDesc(Product::getCreateTime);
        
        Page<Product> result = productMapper.selectPage(pageParam, wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateProduct(Long userId, Long productId, Product product) {
        Product existing = productMapper.selectById(productId);
        if (existing == null) {
            return Result.error(404, "商品不存在");
        }
        if (!existing.getFarmerId().equals(userId)) {
            return Result.error(403, "无权操作此商品");
        }
        
        product.setId(productId);
        product.setFarmerId(userId);
        product.setProductType(1);
        product.setAuditStatus(0);
        product.setStatus(0);
        product.setRejectReason(null);
        productMapper.updateById(product);
        
        return Result.success("修改成功，商品将重新进入审核流程", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteProduct(Long userId, Long productId) {
        Product existing = productMapper.selectById(productId);
        if (existing == null) {
            return Result.error(404, "商品不存在");
        }
        if (!existing.getFarmerId().equals(userId)) {
            return Result.error(403, "无权操作此商品");
        }
        
        productMapper.deleteById(productId);
        return Result.success("删除成功", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateProductStatus(Long userId, Long productId, Integer status) {
        Product existing = productMapper.selectById(productId);
        if (existing == null) {
            return Result.error(404, "商品不存在");
        }
        if (!existing.getFarmerId().equals(userId)) {
            return Result.error(403, "无权操作此商品");
        }
        if (existing.getAuditStatus() != 1) {
            return Result.error("该商品尚未通过审核，无法上下架");
        }
        
        existing.setStatus(status);
        productMapper.updateById(existing);
        return Result.success(status == 1 ? "上架成功" : "下架成功", null);
    }

    @Override
    public Result<Product> getMyProductDetail(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return Result.error(404, "商品不存在");
        }
        if (!product.getFarmerId().equals(userId)) {
            return Result.error(403, "无权查看此商品");
        }
        return Result.success(product);
    }
}
