package com.example.testplatform.controller;

import com.example.testplatform.common.PageResult;
import com.example.testplatform.common.Result;
import com.example.testplatform.entity.TestCase;
import com.example.testplatform.service.TestCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 测试用例接口
 */
@Tag(name = "测试用例管理", description = "测试用例的新增、修改、删除与分页查询")
@RestController
@RequestMapping("/api/cases")
public class TestCaseController {

    private final TestCaseService testCaseService;

    @Autowired
    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    /**
     * 分页查询用例，可按项目、关键字过滤
     */
    @Operation(summary = "分页查询用例", description = "可按项目、关键字过滤")
    @GetMapping
    public Result<PageResult<TestCase>> page(@RequestParam(required = false) String projectId,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(testCaseService.page(projectId, keyword, pageNum, pageSize));
    }

    /**
     * 用例详情
     */
    @Operation(summary = "用例详情")
    @GetMapping("/{id}")
    public Result<TestCase> detail(@PathVariable Long id) {
        return Result.ok(testCaseService.getById(id));
    }

    /**
     * 新增用例
     */
    @Operation(summary = "新增用例")
    @PostMapping
    public Result<TestCase> create(@Valid @RequestBody TestCase testCase) {
        return Result.ok(testCaseService.create(testCase));
    }

    /**
     * 修改用例
     */
    @Operation(summary = "修改用例")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TestCase testCase) {
        testCase.setId(id);
        testCaseService.update(testCase);
        return Result.ok();
    }

    /**
     * 删除用例
     */
    @Operation(summary = "删除用例")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        testCaseService.delete(id);
        return Result.ok();
    }
}
