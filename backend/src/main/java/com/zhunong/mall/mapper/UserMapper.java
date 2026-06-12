package com.zhunong.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhunong.mall.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问层
 *
 * @author 助农商城开发团队
 * @since 2026-03-22
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
