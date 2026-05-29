package com.star.es.handler.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.star.es.domain.entity.User;
import com.star.es.handler.TableSyncHandler;
import com.star.es.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 用户表 canal 同步处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserSyncHandler implements TableSyncHandler {

    private final UserRepository userRepository;

    @Override
    public String getTableName() {
        // 对应 MySQL 表名
        return "user";
    }

    @Override
    public void handleInsert(CanalEntry.RowData rowData) {
        User user = convert(rowData.getAfterColumnsList());
        if (user.getId() == null) {
            log.warn("[user] 新增数据ID为空，跳过同步");
            return;
        }
        userRepository.save(user);
        log.info("[user] 同步新增成功 id={}", user.getId());
    }

    @Override
    public void handleUpdate(CanalEntry.RowData rowData) {
        User user = convert(rowData.getAfterColumnsList());
        if (user.getId() == null) {
            log.warn("[user] 更新数据ID为空，跳过同步");
            return;
        }
        userRepository.save(user);
        log.info("[user] 同步更新成功 id={}", user.getId());
    }

    @Override
    public void handleDelete(CanalEntry.RowData rowData) {
        User user = convert(rowData.getBeforeColumnsList());
        if (user.getId() == null) {
            log.warn("[user] 删除数据ID为空，跳过同步");
            return;
        }
        userRepository.deleteById(user.getId());
        log.info("[user] 同步删除成功 id={}", user.getId());
    }

    /**
     * 字段转换（每张表自己实现）
     */
    private User convert(List<CanalEntry.Column> columns) {
        User user = new User();
        for (CanalEntry.Column column : columns) {
            try {
                String field = column.getName();
                String value = column.getValue();
                if (value == null || value.isEmpty()) continue;

                switch (field.toLowerCase()) {
                    case "id" -> user.setId(Long.valueOf(value));
                    case "name" -> user.setName(value);
                    case "age" -> user.setAge(Integer.valueOf(value));
                    case "email" -> user.setEmail(value);
                }
            } catch (Exception e) {
                log.warn("[user] 字段转换失败 column={}", column.getName(), e);
            }
        }
        return user;
    }
}
