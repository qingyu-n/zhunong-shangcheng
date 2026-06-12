package com.zhunong.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhunong.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@TableName("product_report")
@Data
@EqualsAndHashCode(callSuper = false)
public class ProductReport extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long productId;

    private Long reporterId;

    private Integer reasonType;

    private String reasonDetail;

    private String evidenceImages;

    private Integer status;

    private Long handlerId;

    private String handleResult;

    private LocalDateTime handleTime;
}
