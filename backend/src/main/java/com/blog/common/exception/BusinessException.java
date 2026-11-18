package com.blog.common.exception;

import com.blog.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常：表示 "可预期的业务失败"，比如用户名已存在、密码错误、文章不存在。
 *
 * <p>为什么用抛异常而不是 return Result.error()？
 * <ul>
 *   <li>Service 层方法可以直接返回业务对象（如 LoginVO），不用每层都包一个 Result</li>
 *   <li>出错时 throw 一下就能中断后续逻辑，并且能触发 @Transactional 回滚
 *       （继承 RuntimeException，Spring 事务默认只对运行时异常回滚）</li>
 *   <li>由 {@link GlobalExceptionHandler} 统一捕获，转换成 Result 返回给前端</li>
 * </ul>
 *
 * <p>用法：
 * <pre>
 * throw new BusinessException(ResultCode.USERNAME_EXISTS);
 * throw new BusinessException(ResultCode.BAD_REQUEST, "标签ID不存在：" + tagId);
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务状态码，对应 Result.code */
    private final Integer code;

    /**
     * 使用枚举中预定义的 code 和 message。
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 使用枚举中的 code，但自定义提示信息（需要带上具体数据时用）。
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    /**
     * 只给提示信息，code 默认 400。适合临时、不值得单独定义枚举的错误。
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BAD_REQUEST.getCode();
    }
}
