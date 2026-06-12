package com.zhunong.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhunong.mall.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品数据访问层
 *
 * @author 助农商城开发团队
 * @since 2026-03-22
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
