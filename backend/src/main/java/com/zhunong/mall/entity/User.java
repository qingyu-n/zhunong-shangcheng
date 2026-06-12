package com.zhunong.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhunong.mall.common.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@TableName("sys_user")
@Data
@EqualsAndHashCode(callSuper = false)
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private Integer status;
    private String role;

    private Integer isFarmer;
    private Integer farmerApplyStatus;
    private String realName;
    private String idCard;
}
