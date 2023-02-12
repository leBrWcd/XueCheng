package com.xuecheng.media.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description 分布式文件系统 Minio 配置
 *
 * @author lebrwcd
 * @version 1.0
 * @date 2023/2/12
 */
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder().endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        return client;
    }

}
