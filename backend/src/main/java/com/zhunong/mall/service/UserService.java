package com.zhunong.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhunong.mall.entity.User;

/**
 * 用户服务接口
 *
 * @author 助农商城开发团队
 * @since 2026-03-22
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户信息
     */
    User login(String username, String password);

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return 注册成功的用户
     */
    User register(User user);
}
