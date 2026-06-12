package com.zhunong.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhunong.mall.common.PageResult;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.FarmerProfile;
import com.zhunong.mall.entity.Product;
import com.zhunong.mall.entity.ProductAudit;
import com.zhunong.mall.entity.Shop;
import com.zhunong.mall.mapper.FarmerProfileMapper;
import com.zhunong.mall.mapper.ProductAuditMapper;
import com.zhunong.mall.mapper.ProductMapper;
import com.zhunong.mall.mapper.ShopMapper;
import com.zhunong.mall.mapper.UserMapper;
import com.zhunong.mall.entity.User;
import com.zhunong.mall.service.AuditService;
import com.zhunong.mall.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditServiceImpl extends ServiceImpl<ProductAuditMapper, ProductAudit> implements AuditService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private FarmerProfileMapper farmerProfileMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageService messageService;
    
    @Autowired
    private ShopMapper shopMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ProductAudit> approveProduct(Long productId, Long adminId, String remark) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return Result.error(404, "商品不存在");
        }
        if (product.getAuditStatus() != 0) {
            return Result.error("该商品已审核，无法重复操作");
        }

        product.setAuditStatus(1);
        product.setStatus(1);
        product.setRejectReason(null);
        productMapper.updateById(product);

        ProductAudit audit = new ProductAudit();
        audit.setProductId(productId);
        audit.setFarmerId(product.getFarmerId());
        audit.setAuditorId(adminId);
        audit.setAuditStatus(1);
        audit.setAuditRemark(remark);
        audit.setAuditTime(LocalDateTime.now());
        baseMapper.insert(audit);

        messageService.sendAuditMessage(
            product.getFarmerId(),
            productId,
            "商品审核通过",
            String.format("您发布的商品《%s》已通过审核，现已上架销售。%s", 
                         product.getName(), 
                         remark != null ? "审核备注：" + remark : "")
        );

        return Result.success("审核通过", audit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ProductAudit> rejectProduct(Long productId, Long adminId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error("请输入拒绝原因");
        }
        
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return Result.error(404, "商品不存在");
        }
        if (product.getAuditStatus() != 0) {
            return Result.error("该商品已审核，无法重复操作");
        }

        product.setAuditStatus(2);
        product.setStatus(0);
        product.setRejectReason(reason);
        productMapper.updateById(product);

        ProductAudit audit = new ProductAudit();
        audit.setProductId(productId);
        audit.setFarmerId(product.getFarmerId());
        audit.setAuditorId(adminId);
        audit.setAuditStatus(2);
        audit.setAuditRemark(reason);
        audit.setAuditTime(LocalDateTime.now());
        baseMapper.insert(audit);

        messageService.sendAuditMessage(
            product.getFarmerId(),
            productId,
            "审核未通过",
            String.format("很遗憾，您发布的商品《%s》未通过审核。拒绝原因：%s", 
                         product.getName(), 
                         reason)
        );

        return Result.success("已拒绝", audit);
    }

    @Override
    public Result<PageResult<ProductAudit>> getAuditProductList(Integer page, Integer size, String keyword, Integer auditStatus) {
        Page<ProductAudit> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ProductAudit> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.exists("SELECT 1 FROM product p WHERE p.id = product_audit.product_id AND p.name LIKE {0}", "%" + keyword + "%"));
        }
        wrapper.eq(auditStatus != null, ProductAudit::getAuditStatus, auditStatus)
               .orderByDesc(ProductAudit::getCreateTime);
        
        Page<ProductAudit> result = baseMapper.selectPage(pageParam, wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<FarmerProfile> approveFarmer(Long profileId, Long adminId, String remark) {
        FarmerProfile profile = farmerProfileMapper.selectById(profileId);
        if (profile == null) {
            return Result.error(404, "农户申请不存在");
        }
        if (profile.getStatus() == 1) {
            return Result.error("该用户已是认证农户");
        }

        profile.setStatus(1);
        profile.setAuditRemark(remark);
        profile.setAuditorId(adminId);
        profile.setAuditTime(LocalDateTime.now());
        farmerProfileMapper.updateById(profile);

        User user = userMapper.selectById(profile.getUserId());
        if (user != null) {
            user.setIsFarmer(1);
            user.setFarmerApplyStatus(1);
            userMapper.updateById(user);
        }

        // ============================================
        // 关键修改！强制用农户申请数据覆盖店铺信息！
        // ============================================
        LambdaQueryWrapper<Shop> shopWrapper = new LambdaQueryWrapper<>();
        shopWrapper.eq(Shop::getFarmerId, profile.getUserId());
        Shop existingShop = shopMapper.selectOne(shopWrapper);
        
        // 不管有没有店铺，强制创建/更新并使用农户申请数据！
        if (existingShop == null) {
            Shop newShop = new Shop();
            newShop.setFarmerId(profile.getUserId());
            
            // 核心修改：用 profile.shopName, profile.shopLogo 等
            if (profile.getShopName() != null && !profile.getShopName().trim().isEmpty()) {
                newShop.setName(profile.getShopName());
            } else {
                newShop.setName(user.getNickname() + "的店铺");
            }
            
            newShop.setLogo(profile.getShopLogo());
            newShop.setDescription(profile.getDescription());
            newShop.setContactPhone(profile.getContactPhone());
            
            // 构建完整地址
            StringBuilder address = new StringBuilder();
            if (profile.getProvince() != null) address.append(profile.getProvince());
            if (profile.getCity() != null) address.append(profile.getCity());
            if (profile.getDistrict() != null) address.append(profile.getDistrict());
            if (profile.getDetailAddress() != null) address.append(profile.getDetailAddress());
            if (address.length() > 0) {
                newShop.setAddress(address.toString());
            }
            
            newShop.setViewCount(0);
            newShop.setFavoriteCount(0);
            newShop.setProductCount(0);
            newShop.setStatus(1);
            shopMapper.insert(newShop);
        } else {
            // 强制覆盖已有店铺数据！
            if (profile.getShopName() != null && !profile.getShopName().trim().isEmpty()) {
                existingShop.setName(profile.getShopName());
            }
            if (profile.getShopLogo() != null) {
                existingShop.setLogo(profile.getShopLogo());
            }
            if (profile.getDescription() != null) {
                existingShop.setDescription(profile.getDescription());
            }
            if (profile.getContactPhone() != null) {
                existingShop.setContactPhone(profile.getContactPhone());
            }
            
            // 重构完整地址
            StringBuilder address = new StringBuilder();
            if (profile.getProvince() != null) address.append(profile.getProvince());
            if (profile.getCity() != null) address.append(profile.getCity());
            if (profile.getDistrict() != null) address.append(profile.getDistrict());
            if (profile.getDetailAddress() != null) address.append(profile.getDetailAddress());
            if (address.length() > 0) {
                existingShop.setAddress(address.toString());
            }
            
            existingShop.setStatus(1);
            shopMapper.updateById(existingShop);
        }

        messageService.sendMessage(
            profile.getUserId(),
            "恭喜！您已成为认证农户",
            "您的农户申请已通过审核，现在可以发布农产品了！" + (remark != null ? "备注：" + remark : "")
        );

        return Result.success("审核通过", profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<FarmerProfile> rejectFarmer(Long profileId, Long adminId, String reason) {
        FarmerProfile profile = farmerProfileMapper.selectById(profileId);
        if (profile == null) {
            return Result.error(404, "农户申请不存在");
        }

        profile.setStatus(2);
        profile.setAuditRemark(reason);
        profile.setAuditorId(adminId);
        profile.setAuditTime(LocalDateTime.now());
        farmerProfileMapper.updateById(profile);

        User user = userMapper.selectById(profile.getUserId());
        if (user != null) {
            user.setIsFarmer(0);
            user.setFarmerApplyStatus(2);
            userMapper.updateById(user);
        }

        messageService.sendMessage(
            profile.getUserId(),
            "农户申请未通过",
            "很遗憾，您的农户申请未通过审核。" + (reason != null ? "原因：" + reason : "")
        );

        return Result.success("已拒绝", profile);
    }

    @Override
    public Result<PageResult<FarmerProfile>> getFarmerApplyList(Integer page, Integer size, String keyword, Integer status) {
        Page<FarmerProfile> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<FarmerProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(keyword != null && !keyword.isEmpty(), FarmerProfile::getShopName, keyword)
               .eq(status != null, FarmerProfile::getStatus, status)
               .orderByDesc(FarmerProfile::getCreateTime);
        
        Page<FarmerProfile> result = farmerProfileMapper.selectPage(pageParam, wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal()));
    }
}
