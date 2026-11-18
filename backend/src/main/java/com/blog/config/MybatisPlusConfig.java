package com.blog.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类。
 *
 * <p>{@code @MapperScan}：扫描 com.blog.mapper 包下所有接口，自动生成代理实现类并注册成 Bean，
 * 这样 Mapper 接口上就不用每个都写 @Mapper 了。
 *
 * <p>MyBatis-Plus 的插件机制：一个 {@link MybatisPlusInterceptor} 里装多个 "内部插件"（InnerInterceptor），
 * 每条 SQL 执行前依次经过它们。官方建议顺序：分页 → 防全表更新删除。
 */
@Configuration
@MapperScan("com.blog.mapper")
public class MybatisPlusConfig {

    /**
     * 注册 MyBatis-Plus 插件。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 分页插件：不加它的话 selectPage 不会分页，会把整张表查出来！
        //    原理：拦截查询 SQL，先自动执行一条 SELECT COUNT(*) 得到 total，再在原 SQL 后拼接分页语句
        //    不传数据库类型（DbType），插件会根据 JDBC URL 自动识别 h2 / mysql，选用对应的分页语法，
        //    这样切换 profile 时不用改代码
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor();
        // 单页最大条数，防止前端传 size=100000 把数据库拖垮；超过时按 100 处理
        pagination.setMaxLimit(100L);
        interceptor.addInnerInterceptor(pagination);

        // 2. 防全表更新/删除插件：检测到没有 WHERE 条件的 UPDATE / DELETE 直接报错，防止手滑清空整张表
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }
}
