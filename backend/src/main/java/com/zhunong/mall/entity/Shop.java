package com.zhunong.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhunong.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("shop")
@Data
@EqualsAndHashCode(callSuper = false)
public class Shop extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long farmerId;

    private String name;

    private String logo;

    private String bannerImage;

    private String description;

    private String contactPhone;

    private String contactWechat;

    private String address;

    private Integer viewCount;

    private Integer favoriteCount;

    private Integer productCount;

    private Integer status;
}
