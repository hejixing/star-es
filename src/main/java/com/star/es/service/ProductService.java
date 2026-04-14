package com.star.es.service;


import com.star.es.domain.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ProductService {

    // 新增/修改文档
    Product save(Product product);

    // 根据ID查询
    Optional<Product> findById(Long id);

    // 分词查询名称
    List<Product> findByName(String name);

    // 精确查询分类
    List<Product> findByCategory(String category);

    // 分页查询所有
    Page<Product> findAll(int page, int size);

    // 删除文档
    void deleteById(Long id);
}
