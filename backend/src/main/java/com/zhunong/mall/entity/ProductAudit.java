package com.zhunong.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("product_audit")
@Data
public class ProductAudit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private Long farmerId;

    private Long auditorId;

    private Integer auditStatus;

    private String auditRemark;

    private LocalDateTime auditTime;

    private LocalDateTime createTime;
}
