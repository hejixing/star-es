package com.star.es.controller;

import com.star.es.domain.entity.Product;
import com.star.es.service.ProductService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("es/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 新增/修改
    @PostMapping("/save")
    public Product save(@RequestBody Product product) {
        return productService.save(product);
    }

    // 根据ID查询
    @GetMapping("/get/{id}")
    public Optional<Product> getById(@PathVariable("id") Long id) {
        return productService.findById(id);
    }

    // 名称分词查询
    @GetMapping("/search/name")
    public List<Product> searchByName(@RequestParam("name") String name) {
        return productService.findByName(name);
    }

    // 分类精确查询
    @GetMapping("/search/category")
    public List<Product> searchByCategory(@RequestParam("category") String category) {
        return productService.findByCategory(category);
    }

    // 分页查询
    @GetMapping("/list")
    public Page<Product> list(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size) {
        return productService.findAll(page, size);
    }

    // 删除
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        productService.deleteById(id);
        return "删除成功";
    }
}
