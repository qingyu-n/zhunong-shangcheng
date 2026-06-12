package com.zhunong.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhunong.mall.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类数据访问层
 *
 * @author 助农商城开发团队
 * @since 2026-03-22
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
