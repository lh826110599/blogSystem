package com.blog.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装，作为 {@link Result#getData()} 返回给前端。
 *
 * <p>为什么不直接返回 MyBatis-Plus 的 IPage？
 * IPage 里有 orders、optimizeCountSql、pages 等一堆前端用不到的字段，
 * 自己封装一层只保留前端需要的 4 个字段，接口更干净，也和持久层框架解耦。
 *
 * <p>返回给前端的格式：
 * <pre>
 * { "total": 100, "current": 1, "size": 10, "records": [ ... ] }
 * </pre>
 * Ant Design 的 Table 分页组件正好需要 total / current / pageSize。
 *
 * @param <T> 列表元素类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页码，从 1 开始")
    private Long current;

    @Schema(description = "每页条数")
    private Long size;

    @Schema(description = "当前页数据")
    private List<T> records;

    /**
     * 直接把 MyBatis-Plus 分页对象转换成 PageResult（records 类型不变）。
     *
     * @param page MyBatis-Plus 查询返回的分页对象
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /**
     * 分页信息取自 page，列表数据用转换后的 records。
     * 典型场景：数据库查出 Article 实体，转换成 ArticleVO 后再返回。
     *
     * @param page    MyBatis-Plus 分页对象（只用它的 total / current / size）
     * @param records 转换后的数据列表
     */
    public static <T> PageResult<T> of(IPage<?> page, List<T> records) {
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }
}
