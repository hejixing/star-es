package com.star.es.config;


import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.star.es.properties.CanalProperties;
import java.net.InetSocketAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * canal配置
 * @author Hejx
 */
@Configuration
@RequiredArgsConstructor
public class CanalConfig {

    private final CanalProperties canalProperties;

    /**
     * 创建连接
     * @return CanalConnector
     */
    @Bean
    public CanalConnector canalConnector() {
        return CanalConnectors.newSingleConnector(
            new InetSocketAddress(canalProperties.getHost(), canalProperties.getPort()),
            canalProperties.getDestination(), "", "");
    }
}
