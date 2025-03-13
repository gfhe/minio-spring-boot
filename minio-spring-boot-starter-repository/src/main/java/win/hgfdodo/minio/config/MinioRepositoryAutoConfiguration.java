package win.hgfdodo.minio.config;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import win.hgfdodo.minio.endpoint.MinioEndpoint;
import win.hgfdodo.minio.service.MinioTemplate;

@ConditionalOnProperty(name = "spring.minio.endpoint.enable", havingValue = "true")
@ConditionalOnBean({MinioClient.class, MinioTemplate.class})
@Configuration
public class MinioRepositoryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MinioEndpoint minioEndpoint(MinioTemplate minioTemplate) {
        return new MinioEndpoint(minioTemplate);
    }

}
