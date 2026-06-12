package com.zhunong.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhunong.mall.common.PageResult;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.Message;
import com.zhunong.mall.mapper.MessageMapper;
import com.zhunong.mall.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromHeader(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }

    @GetMapping
    public Result<PageResult<Message>> getMessageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer isRead,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Page<Message> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId)
               .eq(type != null, Message::getType, type)
               .eq(isRead != null, Message::getIsRead, isRead)
               .orderByDesc(Message::getCreateTime);

        Page<Message> result = messageMapper.selectPage(pageParam, wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Message message = messageMapper.selectById(id);
        if (message == null || !message.getUserId().equals(userId)) {
            return Result.error(404, "消息不存在");
        }
        message.setIsRead(1);
        message.setReadTime(LocalDateTime.now());
        messageMapper.updateById(message);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Message update = new Message();
        update.setIsRead(1);
        update.setReadTime(LocalDateTime.now());

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId).eq(Message::getIsRead, 0);
        messageMapper.update(update, wrapper);

        return Result.success();
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId).eq(Message::getIsRead, 0);
        Long count = messageMapper.selectCount(wrapper);
        return Result.success(count);
    }
}
