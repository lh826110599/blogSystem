package com.blog.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一返回结果封装。所有 Controller 接口都返回这个结构，前端只需要处理一种格式：
 * <pre>
 * {
 *   "code": 200,          // 业务状态码，见 {@link ResultCode}
 *   "message": "操作成功", // 提示信息
 *   "data": { ... }        // 业务数据，可以是对象、列表、分页结果，失败时为 null
 * }
 * </pre>
 *
 * <p>用法示例：
 * <pre>
 * return Result.success(articleVO);               // 成功并返回数据
 * return Result.success();                        // 成功但没有数据（如删除）
 * return Result.error(ResultCode.LOGIN_FAILED);   // 失败
 * </pre>
 * 业务代码里更推荐直接 throw new BusinessException(...)，由全局异常处理器统一转成 Result.error。
 *
 * @param <T> data 的类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一返回结果")
public class Result<T> implements Serializable {

    @Schema(description = "业务状态码，200 表示成功", example = "200")
    private Integer code;

    @Schema(description = "提示信息", example = "操作成功")
    private String message;

    @Schema(description = "业务数据")
    private T data;

    // ========================= 成功 =========================

    /**
     * 成功，不携带数据。适用于新增、修改、删除等只需要告诉前端 "成功了" 的接口。
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功，并携带业务数据。
     *
     * @param data 返回给前端的数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功，自定义提示信息并携带数据。例如 "登录成功"。
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // ========================= 失败 =========================

    /**
     * 失败，使用枚举里预定义的 code 和 message。
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 失败，自定义 code 和 message。全局异常处理器里用得最多。
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
