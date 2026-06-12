package com.zhunong.mall.common;

/**
 * 响应状态码枚举
 */
public enum ResultCode {
    
    // 成功状态码
    SUCCESS(200, "操作成功"),
    
    // 客户端错误 1000-1999
    PARAM_ERROR(1000, "参数错误"),
    PARAM_NULL(1001, "参数为空"),
    PARAM_TYPE_ERROR(1002, "参数类型错误"),
    PARAM_FORMAT_ERROR(1003, "参数格式错误"),
    
    // 用户相关错误 2000-2999
    USER_NOT_LOGIN(2000, "用户未登录"),
    USER_NOT_EXIST(2001, "用户不存在"),
    USER_PASSWORD_ERROR(2002, "密码错误"),
    USER_ACCOUNT_DISABLED(2003, "账号已被禁用"),
    USER_ACCOUNT_EXISTS(2004, "账号已存在"),
    USER_PHONE_EXISTS(2005, "手机号已存在"),
    USER_EMAIL_EXISTS(2006, "邮箱已存在"),
    USER_OLD_PASSWORD_ERROR(2007, "原密码错误"),
    USER_TOKEN_EXPIRED(2008, "登录已过期，请重新登录"),
    USER_TOKEN_INVALID(2009, "无效的令牌"),
    USER_NO_PERMISSION(2010, "无权限访问"),
    
    // 商品相关错误 3000-3999
    PRODUCT_NOT_EXIST(3000, "商品不存在"),
    PRODUCT_STOCK_NOT_ENOUGH(3001, "商品库存不足"),
    PRODUCT_OFF_SHELF(3002, "商品已下架"),
    
    // 分类相关错误 4000-4999
    CATEGORY_NOT_EXIST(4000, "分类不存在"),
    CATEGORY_HAS_PRODUCTS(4001, "分类下存在商品，无法删除"),
    CATEGORY_HAS_CHILDREN(4002, "分类下存在子分类，无法删除"),
    
    // 购物车相关错误 5000-5999
    CART_NOT_EXIST(5000, "购物车项不存在"),
    CART_ADD_FAILED(5001, "添加购物车失败"),
    
    // 订单相关错误 6000-6999
    ORDER_NOT_EXIST(6000, "订单不存在"),
    ORDER_CREATE_FAILED(6001, "创建订单失败"),
    ORDER_PAY_FAILED(6002, "支付失败"),
    ORDER_STATUS_ERROR(6003, "订单状态错误"),
    ORDER_CANCEL_FAILED(6004, "取消订单失败"),
    
    // 地址相关错误 7000-7999
    ADDRESS_NOT_EXIST(7000, "地址不存在"),
    ADDRESS_ADD_FAILED(7001, "添加地址失败"),
    
    // 文件上传错误 8000-8999
    FILE_UPLOAD_FAILED(8000, "文件上传失败"),
    FILE_SIZE_EXCEED(8001, "文件大小超过限制"),
    FILE_TYPE_ERROR(8002, "文件类型不支持"),
    
    // 服务器错误 9000-9999
    ERROR(9000, "操作失败"),
    SERVER_ERROR(9001, "服务器内部错误"),
    DATABASE_ERROR(9002, "数据库操作失败"),
    NETWORK_ERROR(9003, "网络请求失败");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getMsg() {
        return message;
    }
}
