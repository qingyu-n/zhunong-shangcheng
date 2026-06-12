package com.zhunong.mall.vo;

/**
 * 订单统计VO
 */
public class OrderStatsVO {
    private Long unpaidCount;      // 待付款
    private Long unshippedCount;   // 待发货
    private Long unreceivedCount;  // 待收货
    private Long completedCount;   // 已完成

    public Long getUnpaidCount() {
        return unpaidCount;
    }

    public void setUnpaidCount(Long unpaidCount) {
        this.unpaidCount = unpaidCount;
    }

    public Long getUnshippedCount() {
        return unshippedCount;
    }

    public void setUnshippedCount(Long unshippedCount) {
        this.unshippedCount = unshippedCount;
    }

    public Long getUnreceivedCount() {
        return unreceivedCount;
    }

    public void setUnreceivedCount(Long unreceivedCount) {
        this.unreceivedCount = unreceivedCount;
    }

    public Long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Long completedCount) {
        this.completedCount = completedCount;
    }
}
