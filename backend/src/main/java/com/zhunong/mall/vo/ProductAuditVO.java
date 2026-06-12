package com.zhunong.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品审核记录视图对象
 */
@Data
public class ProductAuditVO {

    /** 审核记录ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 商品主图 */
    private String productMainImage;

    /** 商品价格 */
    private BigDecimal productPrice;

    /** 农户用户ID */
    private Long farmerId;

    /** 农户昵称 */
    private String farmerName;

    /** 农户头像 */
    private String farmerAvatar;

    /** 分类名称 */
    private String categoryName;

    /** 审核管理员ID */
    private Long auditorId;

    /** 审核管理员名称 */
    private String auditorName;

    /** 审核状态: 0-待审核 1-通过 2-拒绝 */
    private Integer auditStatus;

    /** 审核状态文本 */
    private String auditStatusText;

    /** 审核备注 */
    private String auditRemark;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 提交时间（商品创建时间） */
    private LocalDateTime submitTime;
}
