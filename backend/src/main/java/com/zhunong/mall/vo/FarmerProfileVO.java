package com.zhunong.mall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 农户资料视图对象
 */
@Data
public class FarmerProfileVO {

    /** 主键ID */
    private Long id;

    /** 关联用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 用户昵称 */
    private String nickname;

    /** 用户头像 */
    private String avatar;

    /** 店铺名称 */
    private String shopName;

    /** 店铺Logo URL */
    private String shopLogo;

    /** 店铺简介 */
    private String description;

    /** 营业执照图片URL */
    private String businessLicense;

    /** 联系人姓名 */
    private String contactName;

    /** 联系电话（脱敏显示） */
    private String contactPhone;

    /** 联系地址 */
    private String contactAddress;

    /** 所在省份 */
    private String province;

    /** 所在城市 */
    private String city;

    /** 所在区县 */
    private String district;

    /** 审核状态: 0-待审核 1-已通过 2-已拒绝 */
    private Integer status;

    /** 审核状态文本 */
    private String statusText;

    /** 审核备注 */
    private String auditRemark;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
