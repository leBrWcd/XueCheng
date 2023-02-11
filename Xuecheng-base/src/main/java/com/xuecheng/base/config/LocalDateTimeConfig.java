package com.xuecheng.base.config;/**
 * @author lebrwcd
 * @date 2023/2/5
 * @note
 */

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Description 日期时间序列化配置
 * 2023-02-05T13:28:44.356   -->  2023-02-05 13:32:06
 *
 * @author lebrwcd
 * @version 1.0
 * @date 2023/2/5
 */
@Configuration
public class LocalDateTimeConfig {

    /**
     * 序列化内容
     * LocalDateTime -> String
     * 服务端返回给客户端内容
     * */
    @Bean
    public LocalDateTimeSerializer localDateTimeSerializer() {
        return new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 反序列化内容
     * String -> LocalDateTime
     * 客户端传入服务端数据
     *
     **/
    @Bean
    public LocalDateTimeDeserializer localDateTimeDeserializer() {
        return new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // 配置
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer
    jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.serializerByType(LocalDateTime.class,
                    localDateTimeSerializer());
            builder.deserializerByType(LocalDateTime.class,
                    localDateTimeDeserializer());
        };
    }

}
