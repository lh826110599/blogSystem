package com.blog.common.exception;

import com.blog.common.result.Result;
import com.blog.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 *
 * <p>{@code @RestControllerAdvice} = {@code @ControllerAdvice} + {@code @ResponseBody}：
 * 对所有 Controller 生效，方法返回值直接序列化成 JSON。
 * Controller、Service、拦截器（preHandle）里抛出的异常都会被这里捕获。
 *
 * <p>匹配规则：Spring 会选择 "最具体" 的那个 @ExceptionHandler。
 * 比如抛出 BusinessException，会进 {@link #handleBusinessException}，而不是兜底的 {@link #handleException}。
 *
 * <p>所有方法都返回 HTTP 200 + Result，具体错误看 Result.code。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：代码里主动 throw 的，属于正常业务流程，打 warn 日志即可，不需要堆栈。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 [{} {}] code={}, message={}",
                request.getMethod(), request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验失败（@Valid 校验不通过）。两种接收参数的方式都会进这里（Spring 6 起）：
     * <ul>
     *   <li>POST JSON：{@code @RequestBody @Valid LoginDTO dto}，例如 username 标了 @NotBlank 却没传</li>
     *   <li>GET 查询参数：{@code @Valid ArticleQueryDTO query}，例如 size 超过 @Max(100)</li>
     * </ul>
     * 把所有字段的错误信息拼起来返回，如 "用户名不能为空; 密码长度为6-20位"。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败：{}", message);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 参数绑定失败的兜底处理。
     * MethodArgumentNotValidException 是 BindException 的子类，会优先进上面更具体的方法；
     * 这里接住其他直接抛出 BindException 的情况。
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败：{}", message);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 缺少必填的 @RequestParam 参数。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数：{}", e.getParameterName());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "缺少必填参数：" + e.getParameterName());
    }

    /**
     * 参数类型不匹配。例如 /article/abc，路径参数 id 要求 Long 却传了字符串。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误：{} = {}", e.getName(), e.getValue());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "参数类型错误：" + e.getName());
    }

    /**
     * 请求体无法解析。例如 JSON 格式错误，或者 POST 接口没传请求体。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "请求体格式错误");
    }

    /**
     * 请求方式不支持。例如接口只支持 POST，却用 GET 调用。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方式不支持：{}", e.getMethod());
        return Result.error(ResultCode.METHOD_NOT_ALLOWED);
    }

    /**
     * 接口地址不存在（Spring 6.1+ 找不到处理器时抛出）。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("资源不存在：{}", e.getResourcePath());
        return Result.error(ResultCode.NOT_FOUND);
    }

    /**
     * 兜底：所有没被上面处理的异常（空指针、数据库连接失败等）都到这里。
     * 打 error 日志并带完整堆栈方便排查；返回给前端的只是通用提示，不暴露内部细节。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 [{} {}]", request.getMethod(), request.getRequestURI(), e);
        return Result.error(ResultCode.INTERNAL_ERROR);
    }
}
