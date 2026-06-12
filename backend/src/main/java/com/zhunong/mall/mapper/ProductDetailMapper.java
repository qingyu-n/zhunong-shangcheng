package com.zhunong.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhunong.mall.entity.ProductDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductDetailMapper extends BaseMapper<ProductDetail> {
    
    @Select("SELECT * FROM product_detail WHERE product_id = #{productId} AND is_delete = 0 LIMIT 1")
    ProductDetail selectByProductId(@Param("productId") Long productId);
}
