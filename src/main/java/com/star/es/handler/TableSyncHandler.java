package com.star.es.handler;

import com.alibaba.otter.canal.protocol.CanalEntry;

/**
 *  Canal 表数据同步处理器接口
 *  所有需要同步的表，都必须实现这个接口
 */
public interface TableSyncHandler {

    /**
     * 返回当前处理器要处理的 表名（与MySQL一致）
     */
    String getTableName();

    /**
     * 处理 INSERT 事件
     */
    void handleInsert(CanalEntry.RowData rowData);

    /**
     * 处理 UPDATE 事件
     */
    void handleUpdate(CanalEntry.RowData rowData);

    /**
     * 处理 DELETE 事件
     */
    void handleDelete(CanalEntry.RowData rowData);
}
