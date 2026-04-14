package com.star.es.repository;

import com.star.es.domain.entity.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface ProductRepository extends ElasticsearchRepository<Product, Long> {
    List<Product> findByName(String name);
    List<Product> findByCategory(String category);
}
