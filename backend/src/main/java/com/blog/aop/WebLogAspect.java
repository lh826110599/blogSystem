package com.blog.aop;

import com.blog.common.context.UserContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 接口请求日志切面：自动记录每个 Controller 接口的请求地址、调用方法、当前用户、参数和耗时。
 *
 * <p>AOP 核心概念（对照本类理解）：
 * <ul>
 *   <li>切面 Aspect：本类，"在哪里" + "做什么" 的组合</li>
 *   <li>切点 Pointcut：{@link #controllerPointcut()}，定义 "在哪里" 切入 —— controller 包下所有 public 方法</li>
 *   <li>通知 Advice：{@link #around}，定义 "做什么" —— 方法执行前后打日志、统计耗时</li>
 *   <li>连接点 JoinPoint：被拦截的那次方法调用，可以拿到方法名、参数</li>
 * </ul>
 *
 * <p>日志示例：
 * <pre>
 * ==> POST /api/article | ArticleController.publish | userId=1 | args=[ArticleDTO(title=Hello, ...)]
 * <== POST /api/article | 耗时 35 ms
 * </pre>
 *
 * <p>⚠ 参数通过 toString() 打印，DTO 里的密码等敏感字段必须加 Lombok 的 {@code @ToString.Exclude}，
 * 否则密码会明文出现在日志里。
 */
@Slf4j
@Aspect
@Component
public class WebLogAspect {

    /** 参数日志最大长度，文章正文可能很长，超出部分截断 */
    private static final int MAX_ARGS_LENGTH = 500;

    /**
     * 切点表达式：com.blog.controller 包及子包下，所有类的所有 public 方法。
     * execution(修饰符 返回值 包名.类名.方法名(参数))，".." 表示任意子包 / 任意参数。
     */
    @Pointcut("execution(public * com.blog.controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：包住目标方法，前后都能插入逻辑。
     * 必须调用 joinPoint.proceed() 才会真正执行 Controller 方法，并且要把返回值 return 出去。
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        // 通过 RequestContextHolder 拿到当前请求（Spring 也是用 ThreadLocal 保存的）
        String requestLine = "";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            requestLine = request.getMethod() + " " + request.getRequestURI();
        }

        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();

        log.info("==> {} | {} | userId={} | args={}",
                requestLine, methodName, UserContext.getUserId(), formatArgs(joinPoint.getArgs()));

        try {
            // 执行 Controller 方法
            Object result = joinPoint.proceed();
            log.info("<== {} | 耗时 {} ms", requestLine, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable e) {
            // 只记录一下再原样抛出，交给全局异常处理器处理，切面不吞异常
            log.info("<== {} | 异常 {} | 耗时 {} ms",
                    requestLine, e.getClass().getSimpleName(), System.currentTimeMillis() - start);
            throw e;
        }
    }

    /**
     * 把参数数组转成日志字符串。
     * 过滤掉 request / response / 上传文件对象（打印它们没有意义，有的 toString 还会报错），超长截断。
     */
    private String formatArgs(Object[] args) {
        String text = Arrays.stream(args)
                .filter(arg -> !(arg instanceof ServletRequest
                        || arg instanceof ServletResponse
                        || arg instanceof MultipartFile))
                .map(String::valueOf)
                .collect(Collectors.joining(", ", "[", "]"));
        return text.length() > MAX_ARGS_LENGTH ? text.substring(0, MAX_ARGS_LENGTH) + "...(已截断)" : text;
    }
}
