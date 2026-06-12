package com.zhunong.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.User;
import com.zhunong.mall.mapper.UserMapper;
import com.zhunong.mall.mapper.AddressMapper;
import com.zhunong.mall.mapper.CartMapper;
import com.zhunong.mall.mapper.FavoriteMapper;
import com.zhunong.mall.mapper.OrderMapper;
import com.zhunong.mall.entity.Address;
import com.zhunong.mall.entity.Cart;
import com.zhunong.mall.entity.Favorite;
import com.zhunong.mall.entity.Order;
import com.zhunong.mall.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private OrderMapper orderMapper;

    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return Result.error(401, "未登录");
        }
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getId());
        userInfo.put("userName", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("gender", user.getGender());
        userInfo.put("birthday", user.getBirthday());
        userInfo.put("isFarmer", user.getIsFarmer() != null ? user.getIsFarmer() : 0);
        userInfo.put("farmerApplyStatus", user.getFarmerApplyStatus());
        userInfo.put("roles", user.getRole() != null ? java.util.Arrays.asList(user.getRole().split(",")) : java.util.Arrays.asList("USER"));
        userInfo.put("buttons", new java.util.ArrayList<String>());

        return Result.success(userInfo);
    }

    @GetMapping("/list")
    public Result<Page<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        
        Page<User> page = new Page<>(current, size);
        Page<User> result = userMapper.selectPage(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<User> getUserDetail(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/status")
    public Result<Void> updateUserStatus(@RequestBody User user) {
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setStatus(user.getStatus());
        userMapper.updateById(updateUser);
        return Result.success();
    }

    @PutMapping("/info")
    public Result<Map<String, Object>> updateUserInfo(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                       @RequestBody User userInfo) {
        try {
            if (authHeader == null || authHeader.isEmpty()) {
                return Result.error(401, "未登录");
            }
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);

            User existingUser = userMapper.selectById(userId);
            if (existingUser == null) {
                return Result.error(404, "用户不存在");
            }

            User updateUser = new User();
            updateUser.setId(userId);
            
            if (userInfo.getNickname() != null) updateUser.setNickname(userInfo.getNickname());
            if (userInfo.getPhone() != null) updateUser.setPhone(userInfo.getPhone());
            if (userInfo.getEmail() != null) updateUser.setEmail(userInfo.getEmail());
            if (userInfo.getGender() != null) updateUser.setGender(userInfo.getGender());
            if (userInfo.getAvatar() != null) updateUser.setAvatar(userInfo.getAvatar());
            if (userInfo.getBirthday() != null) updateUser.setBirthday(userInfo.getBirthday());

            userMapper.updateById(updateUser);

            User updatedUser = userMapper.selectById(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("userId", updatedUser.getId());
            result.put("userName", updatedUser.getUsername());
            result.put("nickname", updatedUser.getNickname());
            result.put("email", updatedUser.getEmail());
            result.put("phone", updatedUser.getPhone());
            result.put("avatar", updatedUser.getAvatar());
            result.put("gender", updatedUser.getGender());
            result.put("birthday", updatedUser.getBirthday());
            result.put("isFarmer", updatedUser.getIsFarmer() != null ? updatedUser.getIsFarmer() : 0);
            result.put("farmerApplyStatus", updatedUser.getFarmerApplyStatus());
            result.put("roles", updatedUser.getRole() != null ? java.util.Arrays.asList(updatedUser.getRole().split(",")) : java.util.Arrays.asList("USER"));
            return Result.success("更新成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "更新用户信息失败: " + e.getMessage());
        }
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                       @RequestBody Map<String, String> passwordData) {
        try {
            if (authHeader == null || authHeader.isEmpty()) {
                return Result.error(401, "未登录");
            }
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);

            User existingUser = userMapper.selectById(userId);
            if (existingUser == null) {
                return Result.error(404, "用户不存在");
            }

            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");

            if (currentPassword == null || currentPassword.isEmpty()) {
                return Result.error(400, "请输入当前密码");
            }
            if (newPassword == null || newPassword.length() < 6) {
                return Result.error(400, "新密码至少6位");
            }

            // 验证当前密码
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                    new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            if (!encoder.matches(currentPassword, existingUser.getPassword())) {
                return Result.error(400, "当前密码错误");
            }

            // 更新密码
            User updateUser = new User();
            updateUser.setId(userId);
            updateUser.setPassword(encoder.encode(newPassword));
            userMapper.updateById(updateUser);

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "修改密码失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        try {
            User user = userMapper.selectById(id);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }

            // 先清理关联数据（按顺序：地址 -> 购物车 -> 收藏 -> 订单）
            // 1. 删除用户地址
            try {
                LambdaQueryWrapper<Address> addressWrapper = new LambdaQueryWrapper<>();
                addressWrapper.eq(Address::getUserId, id);
                addressMapper.delete(addressWrapper);
            } catch (Exception e) {
                System.err.println("清理用户地址数据失败: " + e.getMessage());
            }

            // 2. 删除购物车数据
            try {
                LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
                cartWrapper.eq(Cart::getUserId, id);
                cartMapper.delete(cartWrapper);
            } catch (Exception e) {
                System.err.println("清理用户购物车数据失败: " + e.getMessage());
            }

            // 3. 删除收藏数据
            try {
                LambdaQueryWrapper<Favorite> favoriteWrapper = new LambdaQueryWrapper<>();
                favoriteWrapper.eq(Favorite::getUserId, id);
                favoriteMapper.delete(favoriteWrapper);
            } catch (Exception e) {
                System.err.println("清理用户收藏数据失败: " + e.getMessage());
            }

            // 4. 删除订单数据
            try {
                LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
                orderWrapper.eq(Order::getUserId, id);
                orderMapper.delete(orderWrapper);
            } catch (Exception e) {
                System.err.println("清理用户订单数据失败: " + e.getMessage());
            }

            // 最后删除用户
            int result = userMapper.deleteById(id);
            if (result > 0) {
                return Result.success("删除成功");
            } else {
                return Result.error(500, "删除失败，请重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "删除用户失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    public Result<String> batchDeleteUsers(@RequestBody java.util.List<Long> ids) {
        for (Long id : ids) {
            User user = userMapper.selectById(id);
            if (user != null) {
                user.setStatus(0);
                userMapper.updateById(user);
            }
        }
        return Result.success("批量删除成功");
    }
}
