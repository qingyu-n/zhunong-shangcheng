package com.zhunong.mall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 店铺详情视图对象
 */
@Data
public class ShopDetailVO {

    /** 店铺ID */
    private Long id;

    /** 农户用户ID */
    private Long farmerId;

    /** 农户昵称 */
    private String farmerName;

    /** 农户头像 */
    private String farmerAvatar;

    /** 店铺名称 */
    private String name;

    /** 店铺Logo URL */
    private String logo;

    /** 店铺横幅图片URL */
    private String bannerImage;

    /** 店铺简介 */
    private String description;

    /** 联系电话（脱敏显示） */
    private String contactPhone;

    /** 微信号 */
    private String contactWechat;

    /** 店铺地址 */
    private String address;

    /** 访问次数 */
    private Integer viewCount;

    /** 收藏数量 */
    private Integer favoriteCount;

    /** 商品数量 */
    private Integer productCount;

    /** 状态: 0-关闭 1-营业中 */
    private Integer status;

    /** 当前用户是否已收藏 */
    private Boolean isFavorited;

    /** 创建时间 */
    private LocalDateTime createTime;
}
