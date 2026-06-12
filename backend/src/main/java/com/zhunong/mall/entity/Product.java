package com.zhunong.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhunong.mall.common.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@TableName("product")
@Data
@EqualsAndHashCode(callSuper = false)
public class Product extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String name;
    private Long categoryId;
    private String description;
    private String detail;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;
    private String mainImage;
    private String images;
    private String origin;
    private String unit;
    private BigDecimal weight;
    private Integer status;
    private Integer isHot;
    private Integer isNew;
    private Integer isHelp;
    private Integer sortOrder;
    private Long farmerId;
    private Integer productType;
    private Integer auditStatus;
    private String rejectReason;
}
