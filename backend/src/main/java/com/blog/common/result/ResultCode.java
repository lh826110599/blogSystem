package com.blog.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一业务状态码枚举。
 *
 * <p>约定：
 * <ul>
 *   <li>HTTP 状态码统一返回 200，真正的成功/失败看响应体里的 code</li>
 *   <li>200 表示成功；4xx / 5xx 沿用 HTTP 语义表示通用错误</li>
 *   <li>1xxx 用户模块、2xxx 分类标签模块、3xxx 文章模块 —— 按模块分段，看到 code 就知道是哪块出的问题</li>
 * </ul>
 * 前端 Axios 拦截器就是根据这里的 code 判断：200 放行、401 跳登录页、其他弹出 message。
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // ===================== 通用 =====================
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期，请重新登录"),
    FORBIDDEN(403, "没有访问权限"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方式不支持"),
    INTERNAL_ERROR(500, "系统繁忙，请稍后再试"),

    // ===================== 用户模块 1xxx =====================
    USERNAME_EXISTS(1001, "用户名已存在"),
    LOGIN_FAILED(1002, "用户名或密码错误"),
    ACCOUNT_DISABLED(1003, "账号已被禁用"),

    // ===================== 分类 / 标签模块 2xxx =====================
    CATEGORY_EXISTS(2001, "分类名称已存在"),
    CATEGORY_NOT_FOUND(2002, "分类不存在"),
    CATEGORY_HAS_ARTICLE(2003, "该分类下还有文章，不能删除"),
    TAG_EXISTS(2101, "标签名称已存在"),
    TAG_NOT_FOUND(2102, "标签不存在"),

    // ===================== 文章模块 3xxx =====================
    ARTICLE_NOT_FOUND(3001, "文章不存在"),
    ARTICLE_NO_PERMISSION(3002, "只能操作自己发布的文章");

    /** 业务状态码 */
    private final Integer code;

    /** 默认提示信息 */
    private final String message;
}
