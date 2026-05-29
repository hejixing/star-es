package com.star.es.service.impl;


import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.star.es.domain.dto.UserSearchDTO;
import com.star.es.domain.entity.User;
import com.star.es.repository.UserRepository;
import com.star.es.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Page<User> findAll(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * 根据多条件动态搜索用户
     * 支持姓名模糊查询、年龄精确/范围查询、邮箱精确查询
     *
     * @param searchDTO 搜索条件DTO，包含姓名、年龄、邮箱、分页和排序信息
     * @return 分页的用户列表
     */
    @Override
    public Page<User> searchByConditions(UserSearchDTO searchDTO) {
        // 创建must查询条件列表（所有条件必须满足，相当于SQL的AND）
        List<Query> mustQueries = new ArrayList<>();

        // 条件1：姓名模糊查询（使用multiMatch支持中文分词和拼写容错）
        if (searchDTO.getName() != null && !searchDTO.getName().isEmpty()) {
            Query nameQuery = Query.of(q -> q
                .multiMatch(m -> m
                    .query(searchDTO.getName())              // 搜索关键词
                    .fields("name")                          // 搜索字段（User实体配置了ik_max_word分词器）
                    .type(
                        co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields) // 最佳字段匹配策略
                    .fuzziness("AUTO")                       // 自动模糊匹配，允许拼写错误
                )
            );
            mustQueries.add(nameQuery);
        }

        // 条件2：年龄精确查询（term查询不分词，适合数字类型精确匹配）
        if (searchDTO.getAge() != null) {
            Query ageQuery = Query.of(q -> q
                .term(t -> t
                    .field("age")                            // 年龄字段
                    .value(searchDTO.getAge())               // 精确匹配指定年龄
                )
            );
            mustQueries.add(ageQuery);
        }

        // 条件3：年龄范围查询（支持最小值、最大值或两者组合）
        if (searchDTO.getMinAge() != null || searchDTO.getMaxAge() != null) {
            Query rangeQuery = Query.of(q -> q
                .range(r -> {
                    r.field("age");                              // 范围查询的字段
                    if (searchDTO.getMinAge() != null) {
                        r.gte(JsonData.of(searchDTO.getMinAge())); // 大于等于最小年龄
                    }
                    if (searchDTO.getMaxAge() != null) {
                        r.lte(JsonData.of(searchDTO.getMaxAge())); // 小于等于最大年龄
                    }
                    return r;
                })
            );
            mustQueries.add(rangeQuery);
        }

        // 条件4：邮箱精确查询（Keyword类型不分词，完全匹配）
        if (searchDTO.getEmail() != null && !searchDTO.getEmail().isEmpty()) {
            Query emailQuery = Query.of(q -> q
                .term(t -> t
                    .field("email")                          // 邮箱字段（Keyword类型）
                    .value(searchDTO.getEmail())             // 完全匹配邮箱地址
                )
            );
            mustQueries.add(emailQuery);
        }

        // 将所有条件组合成Bool查询（must表示所有条件都必须满足）
        Query boolQuery = Query.of(q -> q
            .bool(b -> b.must(mustQueries))                     // 如果mustQueries为空，则匹配所有文档
        );

        // 构建原生查询对象，设置查询条件和分页排序
        NativeQuery nativeQuery = NativeQuery.builder()
            .withQuery(boolQuery)                                // 设置上面构建的bool查询
            .withPageable(searchDTO.toPageable())                // 设置分页参数（页码、每页大小）和排序（字段、方向）
            .build();

        // 执行Elasticsearch搜索，返回SearchHits（包含文档列表和元数据）
        SearchHits<User> searchHits = elasticsearchOperations.search(nativeQuery, User.class);

        // 将搜索结果转换为Spring Data的Page对象（标准分页响应格式）
        return PageableExecutionUtils.getPage(
            searchHits.stream()
                .map(hit -> hit.getContent())                // 从SearchHit中提取User实体对象
                .toList(),                                   // 转换为List集合
            searchDTO.toPageable(),                              // 分页信息（页码、大小、排序）
            () -> elasticsearchOperations.count(nativeQuery, User.class)
            // 懒加载总记录数（仅在访问total时执行，提升性能）
        );
    }
}
