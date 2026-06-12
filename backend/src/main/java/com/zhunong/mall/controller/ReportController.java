package com.zhunong.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhunong.mall.common.PageResult;
import com.zhunong.mall.common.Result;
import com.zhunong.mall.entity.ProductReport;
import com.zhunong.mall.mapper.ProductReportMapper;
import com.zhunong.mall.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ProductReportMapper productReportMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromHeader(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }

    @PostMapping("/product/{productId}")
    public Result<ProductReport> reportProduct(@PathVariable Long productId,
                                               @RequestBody ProductReport report,
                                               @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long reporterId = getUserIdFromHeader(authHeader);
        if (reporterId == null) {
            return Result.error(401, "请先登录");
        }
        report.setProductId(productId);
        report.setReporterId(reporterId);
        report.setStatus(0);
        report.setCreateTime(LocalDateTime.now());
        productReportMapper.insert(report);
        return Result.success("举报提交成功，感谢您的反馈", report);
    }

    @GetMapping
    public Result<PageResult<ProductReport>> getReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Page<ProductReport> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ProductReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, ProductReport::getStatus, status)
               .orderByDesc(ProductReport::getCreateTime);
        
        Page<ProductReport> result = productReportMapper.selectPage(pageParam, wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal()));
    }

    @PutMapping("/{id}/handle")
    public Result<Void> handleReport(@PathVariable Long id,
                                      @RequestBody ProductReport handleInfo,
                                      @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long handlerId = getUserIdFromHeader(authHeader);
        if (handlerId == null) {
            return Result.error(401, "请先登录");
        }
        ProductReport report = productReportMapper.selectById(id);
        if (report == null) {
            return Result.error(404, "举报记录不存在");
        }
        
        report.setStatus(handleInfo.getStatus());
        report.setHandlerId(handlerId);
        report.setHandleResult(handleInfo.getHandleResult());
        report.setHandleTime(LocalDateTime.now());
        productReportMapper.updateById(report);
        
        return Result.success("处理完成", null);
    }
}
