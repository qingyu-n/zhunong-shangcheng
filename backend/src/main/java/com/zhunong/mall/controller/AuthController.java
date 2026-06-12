package com.zhunong.mall.controller;

import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.User;
import com.zhunong.mall.service.UserService;
import com.zhunong.mall.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 *
 * @author 助农商城开发团队
 * @since 2026-03-22
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     *
     * @param loginForm 登录表单
     * @return Token和用户信息
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginForm) {
        String username = loginForm.get("username");
        String password = loginForm.get("password");

        User user = userService.login(username, password);

        // 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.refreshToken(token);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("userInfo", user);

        return Result.success("登录成功", result);
    }

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return Token和用户信息
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody User user) {
        User registeredUser = userService.register(user);

        // 生成JWT token
        String token = jwtUtil.generateToken(registeredUser.getId(), registeredUser.getUsername(), registeredUser.getRole());
        String refreshToken = jwtUtil.refreshToken(token);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("userInfo", registeredUser);

        return Result.success("注册成功", result);
    }
}
