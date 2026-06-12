package com.zhunong.mall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息视图对象
 */
@Data
public class MessageVO {

    /** 消息ID */
    private Long id;

    /** 接收用户ID */
    private Long userId;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 消息类型: 1-系统通知 2-审核通知 3-订单通知 */
    private Integer type;

    /** 消息类型文本 */
    private String typeText;

    /** 关联业务ID（如商品ID、订单ID） */
    private Long relatedId;

    /** 关联业务类型 */
    private String relatedType;

    /** 是否已读: 0-未读 1-已读 */
    private Integer isRead;

    /** 阅读时间 */
    private LocalDateTime readTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
