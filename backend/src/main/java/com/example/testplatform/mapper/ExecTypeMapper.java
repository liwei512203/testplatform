package com.example.testplatform.mapper;

import java.util.List;

/**
 * 执行方式字典 Mapper
 */
public interface ExecTypeMapper {

    /**
     * 全部执行方式（按 sort 排序）
     */
    List<String> selectAll();
}
