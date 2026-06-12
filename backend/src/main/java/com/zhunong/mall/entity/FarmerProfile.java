package com.zhunong.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhunong.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@TableName("farmer_profile")
@Data
@EqualsAndHashCode(callSuper = false)
public class FarmerProfile extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String shopName;

    private String shopLogo;

    private String description;

    private String businessLicense;

    private String contactName;

    private String contactPhone;

    private String contactAddress;

    private String province;

    private String city;

    private String district;

    private String detailAddress;

    private Integer status;

    private String auditRemark;

    private Long auditorId;

    private LocalDateTime auditTime;
}
