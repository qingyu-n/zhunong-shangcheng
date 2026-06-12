package com.zhunong.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("message")
@Data
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private Integer type;

    private Long relatedId;

    private String relatedType;

    private Integer isRead;

    private LocalDateTime readTime;

    private LocalDateTime createTime;
}
