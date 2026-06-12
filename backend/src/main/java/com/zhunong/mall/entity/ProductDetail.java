package com.zhunong.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_detail")
public class ProductDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long productId;
    
    private String description;
    
    private String farmerStory;
    
    private String farmerImage;
    
    private String image1;
    
    private String image2;
    
    private String image3;
    
    private String image4;
    
    private String image5;
    
    private String paramName;
    
    private String paramOrigin;
    
    private String paramShelfLife;
    
    private String paramStorage;
    
    private String paramDelivery;
    
    private Integer status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private Integer isDelete;
}
