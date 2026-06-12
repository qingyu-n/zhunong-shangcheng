package com.zhunong.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhunong.mall.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项Mapper接口
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
