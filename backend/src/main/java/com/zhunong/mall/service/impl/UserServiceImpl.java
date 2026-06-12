package com.zhunong.mall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhunong.mall.entity.User;
import com.zhunong.mall.exception.BusinessException;
import com.zhunong.mall.mapper.UserMapper;
import com.zhunong.mall.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 * @author 助农商城开发团队
 * @since 2026-03-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User login(String username, String password) {
        User user = lambdaQuery()
                .eq(User::getUsername, username)
                .one();

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        return user;
    }

    @Override
    public User register(User user) {
        // 检查用户名是否已存在
        long count = lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 加密密码
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);

        // 设置默认角色
        if (user.getRole() == null) {
            user.setRole("USER");
        }

        // 设置默认状态
        user.setStatus(1);

        save(user);
        return user;
    }
}
