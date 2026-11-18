package com.blog.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类：自定义 RedisTemplate 的序列化方式。
 *
 * <p>为什么要自定义？
 * Spring Boot 默认提供的 RedisTemplate&lt;Object, Object&gt; 使用 JDK 序列化，
 * 存进 Redis 的 key 会变成 "\xac\xed\x00\x05t\x00\x0f..." 这种乱码，用工具根本看不懂，
 * 而且要求对象实现 Serializable，跨语言也读不了。
 *
 * <p>改造后：
 * <ul>
 *   <li>key / hashKey：String 序列化 → 在 Redis 里看到的就是 blog:login:token:1</li>
 *   <li>value / hashValue：JSON 序列化 → 存对象时是可读的 JSON，并带上 "@class" 类型信息，取出来能还原成原来的 Java 类型</li>
 * </ul>
 *
 * <p>Bean 方法名叫 redisTemplate，会替换掉 Spring Boot 自动配置的同名 Bean。
 * 使用时直接注入：{@code private final RedisTemplate<String, Object> redisTemplate;}
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 连接工厂由 Spring Boot 根据 spring.data.redis.* 配置自动创建（默认 Lettuce）
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis 专用的 ObjectMapper（不影响 Spring MVC 返回 JSON 用的那个）。
     */
    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 支持 Java 8 时间类型 LocalDateTime，并序列化成 "2024-01-01T10:00:00" 字符串而不是时间戳数组
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 序列化时写入 "@class" 类型信息，反序列化时才知道要还原成哪个类。
        // 安全：只允许反序列化本项目和 JDK 常用类型。如果放开所有类型（LaissezFaire），
        // 一旦 Redis 被别人写入恶意数据，可能被利用来执行任意代码（Jackson 反序列化漏洞）。
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.blog.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.time.")
                .allowIfSubType("java.lang.")
                .build();
        mapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        return mapper;
    }
}
