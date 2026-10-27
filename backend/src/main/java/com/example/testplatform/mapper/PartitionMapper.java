package com.example.testplatform.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 分区维护 Mapper（information_schema 查询 + DDL 执行）
 * DDL 语句由 PartitionMaintenanceTask 内部拼接（非用户输入），使用 ${} 原样执行
 */
public interface PartitionMapper {

    /**
     * 查询某表的全部分区信息（partitionName / partitionDescription）
     */
    List<Map<String, Object>> selectPartitions(@Param("tableName") String tableName);

    /**
     * 执行 DDL（建分区 / 删分区）
     */
    int executeDdl(@Param("ddl") String ddl);
}
