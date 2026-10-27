package com.example.testplatform.controller;

import com.example.testplatform.common.Result;
import com.example.testplatform.mapper.ExecTypeMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典接口：值域由数据库字典表驱动，新增值无需改代码
 */
@Tag(name = "字典管理", description = "值域由数据库字典表驱动，新增值无需改代码")
@RestController
@RequestMapping("/api/dicts")
public class DictController {

    private final ExecTypeMapper execTypeMapper;

    @Autowired
    public DictController(ExecTypeMapper execTypeMapper) {
        this.execTypeMapper = execTypeMapper;
    }

    /**
     * 执行方式列表（按字典排序）
     */
    @Operation(summary = "执行方式列表", description = "按字典排序返回执行方式可选值")
    @GetMapping("/exec-types")
    public Result<List<String>> execTypes() {
        return Result.ok(execTypeMapper.selectAll());
    }
}
