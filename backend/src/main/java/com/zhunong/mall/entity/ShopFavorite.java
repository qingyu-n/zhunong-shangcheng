package com.zhunong.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("shop_favorite")
@Data
public class ShopFavorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long shopId;

    private LocalDateTime createTime;
}
