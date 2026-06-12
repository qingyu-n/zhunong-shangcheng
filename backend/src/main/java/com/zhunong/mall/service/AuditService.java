package com.zhunong.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.common.PageResult;
import com.zhunong.mall.entity.FarmerProfile;
import com.zhunong.mall.entity.ProductAudit;

public interface AuditService extends IService<ProductAudit> {

    Result<ProductAudit> approveProduct(Long productId, Long adminId, String remark);

    Result<ProductAudit> rejectProduct(Long productId, Long adminId, String reason);

    Result<PageResult<ProductAudit>> getAuditProductList(Integer page, Integer size, String keyword, Integer auditStatus);

    Result<FarmerProfile> approveFarmer(Long profileId, Long adminId, String remark);

    Result<FarmerProfile> rejectFarmer(Long profileId, Long adminId, String reason);

    Result<PageResult<FarmerProfile>> getFarmerApplyList(Integer page, Integer size, String keyword, Integer status);
}
