package com.zhunong.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhunong.mall.common.PageResult;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.ProductReview;
import com.zhunong.mall.mapper.ProductReviewMapper;
import com.zhunong.mall.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ProductReviewMapper productReviewMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public Result<ProductReview> createReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ProductReview review) {
        try {
            if (authHeader != null && !authHeader.isEmpty()) {
                String token = authHeader.replace("Bearer ", "");
                Long userId = jwtUtil.getUserIdFromToken(token);
                review.setUserId(userId);
            }
            review.setStatus(1);
            review.setCreateTime(LocalDateTime.now());
            productReviewMapper.insert(review);
            return Result.success("评价成功", review);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "评价失败: " + e.getMessage());
        }
    }

    @GetMapping("/product/{productId}")
    public Result<PageResult<ProductReview>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Page<ProductReview> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductReview::getProductId, productId)
                   .eq(ProductReview::getStatus, 1)
                   .orderByDesc(ProductReview::getCreateTime);

            Page<ProductReview> result = productReviewMapper.selectPage(pageParam, wrapper);
            return Result.success(PageResult.of(result.getRecords(), result.getTotal()));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取评价列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/reply")
    public Result<Void> replyReview(@PathVariable Long id,
                                    @RequestBody ProductReview review,
                                    Authentication authentication) {
        try {
            ProductReview existing = productReviewMapper.selectById(id);
            if (existing == null) {
                return Result.error(404, "评价不存在");
            }

            existing.setReplyContent(review.getReplyContent());
            existing.setReplyTime(LocalDateTime.now());
            productReviewMapper.updateById(existing);

            return Result.success("回复成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "回复失败: " + e.getMessage());
        }
    }
}
