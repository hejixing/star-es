package com.star.es.service.impl;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.star.es.handler.TableSyncHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 生产级：单 Canal 服务 + 多表分发
 * 作用：
 * 1. 统一连接 Canal
 * 2. 统一拉取 binlog
 * 3. 根据表名自动分发到对应 TableSyncHandler
 * 4. 统一异常、ack、回滚、优雅停机
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CanalSyncService {

    // ====================== 配置 ======================
    private static final int BATCH_SIZE = 1000;
    private static final long SLEEP_MS = 1000;
    private static final long SHUTDOWN_TIMEOUT = 10000;

    // ====================== 依赖 ======================
    private final CanalConnector canalConnector;

    /**
     * 自动注入所有表处理器
     * key：表名
     * value：对应处理器
     */
    private final List<TableSyncHandler> handlerList;
    private Map<String, TableSyncHandler> handlerMap;

    // ====================== 运行状态 ======================
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executorService;

    /**
     * 初始化：把所有处理器注册到 Map 中
     */
    @PostConstruct
    public void init() {
        handlerMap = handlerList.stream()
            .collect(Collectors.toMap(
                TableSyncHandler::getTableName,
                h -> h
            ));
        log.info("=== 已加载 Canal 表同步处理器：{} 个 ===", handlerMap.size());
        handlerMap.keySet().forEach(table -> log.info("→ 同步表：{}", table));
        start();
    }

    /**
     * 启动 Canal 监听
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            executorService = new ThreadPoolExecutor(
                1, 1, 0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1),
                r -> new Thread(r, "canal-main-thread"),
                new ThreadPoolExecutor.CallerRunsPolicy()
            );
            executorService.execute(this::processLoop);
            log.info("=== Canal 主服务已启动 ===");
        }
    }

    /**
     * 核心循环：拉取 binlog
     */
    private void processLoop() {
        try {
            canalConnector.connect();
            canalConnector.subscribe(".*\\..*");
            canalConnector.rollback();
            log.info("Canal 连接成功，开始监听所有表变更");

            while (running.get()) {
                try {
                    Message message = canalConnector.getWithoutAck(BATCH_SIZE);
                    long batchId = message.getId();
                    List<CanalEntry.Entry> entries = message.getEntries();

                    if (batchId == -1 || entries.isEmpty()) {
                        Thread.sleep(SLEEP_MS);
                        continue;
                    }

                    // 处理数据
                    handleEntries(entries);
                    // 确认批次
                    canalConnector.ack(batchId);

                } catch (InterruptedException e) {
                    log.warn("Canal 线程中断，准备退出");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("批次处理异常，自动回滚", e);
                    canalConnector.rollback();
                    Thread.sleep(SLEEP_MS);
                }
            }
        } catch (Exception e) {
            log.error("Canal 服务异常退出", e);
        } finally {
            disconnect();
            log.info("=== Canal 服务已停止 ===");
        }
    }

    /**
     * 统一处理数据，并根据表名分发
     */
    private void handleEntries(List<CanalEntry.Entry> entries) throws Exception {
        for (CanalEntry.Entry entry : entries) {
            if (entry.getEntryType() != CanalEntry.EntryType.ROWDATA) continue;

            String table = entry.getHeader().getTableName();
            TableSyncHandler handler = handlerMap.get(table);

            // 无对应处理器直接跳过
            if (handler == null) continue;

            // 解析事件
            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            CanalEntry.EventType eventType = rowChange.getEventType();

            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                try {
                    switch (eventType) {
                        case INSERT -> handler.handleInsert(rowData);
                        case UPDATE -> handler.handleUpdate(rowData);
                        case DELETE -> handler.handleDelete(rowData);
                    }
                } catch (Exception e) {
                    log.error("表[{}]单条数据处理失败", table, e);
                }
            }
        }
    }

    /**
     * 优雅停机
     */
    @PreDestroy
    public void stop() {
        log.info("=== 正在关闭 Canal 服务 ===");
        running.set(false);
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    private void disconnect() {
        try {
            canalConnector.disconnect();
        } catch (Exception e) {
            log.error("断开 Canal 连接异常", e);
        }
    }
}
