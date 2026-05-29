package com.star.es.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author YJSIR
 * @since 2025/1/15 9:35
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "canal")
public class CanalProperties {

    /**
     * ip
     */
    private String host;

    /**
     * port
     */
    private Integer port;

    /**
     * 实例
     */
    private String destination;

}
