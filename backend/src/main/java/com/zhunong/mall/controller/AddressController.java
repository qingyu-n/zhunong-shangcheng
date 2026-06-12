package com.zhunong.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.Address;
import com.zhunong.mall.mapper.AddressMapper;
import com.zhunong.mall.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址控制器
 */
@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取用户地址列表
     */
    @GetMapping("/list")
    public Result<List<Address>> getAddressList(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.eq(Address::getIsDelete, 0);
        wrapper.orderByDesc(Address::getIsDefault);
        wrapper.orderByDesc(Address::getCreateTime);
        
        List<Address> list = addressMapper.selectList(wrapper);
        return Result.success(list);
    }

    /**
     * 获取默认地址
     */
    @GetMapping("/default")
    public Result<Address> getDefaultAddress(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.eq(Address::getIsDefault, 1);
        wrapper.eq(Address::getIsDelete, 0);
        
        Address address = addressMapper.selectOne(wrapper);
        return Result.success(address);
    }

    /**
     * 添加收货地址
     */
    @PostMapping
    public Result<Address> addAddress(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Address address) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        address.setUserId(userId);
        
        // 如果设置为默认地址，先将其他地址设为非默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Address::getUserId, userId);
            wrapper.eq(Address::getIsDefault, 1);
            
            Address defaultAddress = addressMapper.selectOne(wrapper);
            if (defaultAddress != null) {
                defaultAddress.setIsDefault(0);
                addressMapper.updateById(defaultAddress);
            }
        }

        addressMapper.insert(address);
        return Result.success(address);
    }

    /**
     * 更新收货地址
     */
    @PutMapping("/{id}")
    public Result<Void> updateAddress(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Address address) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        Address existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error(404, "地址不存在");
        }

        address.setId(id);
        address.setUserId(userId);
        
        // 如果设置为默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Address::getUserId, userId);
            wrapper.eq(Address::getIsDefault, 1);
            wrapper.ne(Address::getId, id);
            
            Address defaultAddress = addressMapper.selectOne(wrapper);
            if (defaultAddress != null) {
                defaultAddress.setIsDefault(0);
                addressMapper.updateById(defaultAddress);
            }
        }

        addressMapper.updateById(address);
        return Result.success();
    }

    /**
     * 删除收货地址
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        Address existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error(404, "地址不存在");
        }

        existing.setIsDelete(1);
        addressMapper.updateById(existing);
        return Result.success();
    }

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        try {
            String token = authHeader.replace("Bearer ", "");
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}
