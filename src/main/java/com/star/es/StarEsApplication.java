package com.star.es;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
// 开启 Elasticsearch Repository 扫描（企业必备）
@EnableElasticsearchRepositories(basePackages = "com.star.es.domain.entity")
public class StarEsApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarEsApplication.class, args);
    }

}
